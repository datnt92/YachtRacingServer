package com.duduto.yacht.model;

import com.duduto.Global;
import com.duduto.yacht.enums.GameState;
import com.duduto.util.DateUtil;
import com.duduto.util.MessagingHelper;
import com.duduto.util.RandomUtil;
import com.electrotank.electroserver5.extensions.api.PluginApi;
import com.electrotank.electroserver5.extensions.api.value.EsObject;
import com.electrotank.electroserver5.extensions.api.value.EsObjectRO;
import com.netgame.database.DatabaseController;
import com.netgame.lobby.enums.Field;
import com.netgame.lobby.model.TrackRace;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamePlayer {

    private PluginApi _api;
    private GameState _gameState;
    public DatabaseController _dbController;
    private List<Player> _lstPlayerTrackRace;
    private List<Player> _lstPlayerPlaying;
    private Map<Integer, BettingYacht> _map;
    private int firstYacht = 0;
    private int secondYacht = 0;
    private int thirdYacht = 0;
    private long timeNextGame = 0;
    private int _GameID;
    private int _countWinner;
    private int _countThirdWin;
    private int _countSecondWin;
    private TrackRace _track;
    private int arrRate[];

    private GamePlayer() {
    }
    private static GamePlayer gamePlayer = null;
    private static final Logger logger = LoggerFactory.getLogger(GamePlayer.class);

    public static GamePlayer getInstance() {
        if (gamePlayer == null) {
            gamePlayer = new GamePlayer();
        }
        return gamePlayer;
    }

    /**
     * when start server, initialize GamePlayer
     *
     * @param ignored
     * @param api
     */
    public void initGamePlayer(EsObjectRO ignored, PluginApi api, int gameId) {
        this._api = api;
        this._GameID = gameId; //for the first run
        this._gameState = GameState.Waiting;
        this._dbController = (DatabaseController) api.acquireManagedObject("DatabaseControllerFactory", null);
        this._lstPlayerPlaying = new ArrayList<Player>();
        //list player view racing online
        this.setArrRate(randomRate());
        this._lstPlayerTrackRace = new ArrayList<Player>();
        this.setTimeCloseGame(_dbController.getTimeRemain());
        logger.info("GamePlayer initialized...");
    }

    /**
     * start play game start by a cron job schedule
     */
    public void startGamePlay() {
        _lstPlayerPlaying = _dbController.getListPlayerPlay(_GameID);
        this.buildHorseMap();
        try {
            this.lookupBestHorse();
            _dbController.updateGameState(_GameID, GameState.Started.getState());
            this.setGameState(GameState.Started);
            _dbController.setNewGame("GAME SESSION " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    DateUtil.getCurrentDateTime(), DateUtil.nextMinutes(Global.MINUTES_PER_SESSION), 0, 0);
            this.setTimeCloseGame(_dbController.getTimeRemain());
            logger.info("GAME " + _GameID + " started...");
            //logger.info("BET TOTAL: " + lstPlayerPlay_.size());
            logger.info("first YACHT: " + firstYacht);
            logger.info("second YACHT: " + secondYacht);
            logger.info("third YACHT: " + thirdYacht);
            //run game
            this.broadcastGameState();
            Thread.sleep(1000L * 4);
            _track = new TrackRace(_api, firstYacht, secondYacht, thirdYacht);
            _track.startGame();
            Thread.sleep(1000L * 20L);
        } catch (Exception e) {
            _api.getLogger().error(e.getMessage(), e);
        } finally {
            logger.info("END GAME " + _GameID);
            this.setGameState(GameState.EndGame);
            this.broadcastGameState();
            MessagingHelper.sendMessageEndGame(firstYacht, secondYacht, thirdYacht, _api);
            this.updateEndGame();
            _dbController.updateGameState(_GameID, GameState.EndGame.getState());
            if (_countWinner > 0 || _countThirdWin > 0 || _countSecondWin > 0) {
                this.sendToWinner();
                ///this.sendToLoser();
            }
            this.reSetEndGame();
        }
    }

    private void updateEndGame() {
        if (isMapNotNull()) {
            if (_map.get(this.firstYacht) != null) {
                _countWinner = _map.get(this.firstYacht).getCountSelect();
            }
            if (_map.get(this.secondYacht) != null) {
                _countSecondWin = _map.get(this.secondYacht).getCountSelect();
            }
            if (_map.get(this.thirdYacht) != null) {
                _countThirdWin = _map.get(this.thirdYacht).getCountSelect();
            }
        }
        _dbController.updateEndGame(_GameID, _countWinner, _countSecondWin, _countThirdWin,
                firstYacht, secondYacht, thirdYacht);
    }

    private void sendToWinner() {
        writeLogWin();
        List<Map<String, Object>> lst = _dbController.getMoneyBonus(_GameID);
        for (Map<String, Object> map : lst) {
            int money = Integer.valueOf(map.get("bonus").toString());
            String username = map.get("username").toString();
            MessagingHelper.sendMessageGameResultToPlayer(username, true, money, _api);
        }
    }

    private void writeLogWin() {
        if (_map.get(this.firstYacht) != null) {
            LinkedList<Player> lstPlayerWinner = _map.get(this.firstYacht).getListPlayerPlay();
            for (Player winner : lstPlayerWinner) {
                winner = _dbController.getPlayerData(winner.getUserName());
                List<Map<String, Object>> lstWin = _dbController.getListWinner(_GameID, firstYacht);
                int bonus = 0;
                int money = 0;
                for (Map<String, Object> map : lstWin) {
                    int rate = (Integer) map.get("rate");
                    money = _dbController.getMoneyBettingOfPlayer(_GameID, winner.getUserName(), firstYacht);
                    bonus = (rate * money * Global.BONUS_MONEY_FIRST) / 100 + money;
                }
                _dbController.updateMoneyResult(_GameID, winner.getUserName(), firstYacht, bonus);
                _dbController.setMoney(winner.getUserName(), winner.getMoney(), bonus,
                        Global.SERVER_NAME, "win 1st in game id: " + _GameID);
                _dbController.writeLogWin(_GameID, winner.getUserName(), firstYacht, 0, 0, bonus);
            }
        }
        if (_map.get(this.secondYacht) != null) {
            LinkedList<Player> lstPlayer2nd = _map.get(this.secondYacht).getListPlayerPlay();
            for (Player winner : lstPlayer2nd) {
                winner = _dbController.getPlayerData(winner.getUserName());
                List<Map<String, Object>> lstWin = _dbController.getListWinner(_GameID, secondYacht);
                int bonus = 0;
                int money = 0;
                for (Map<String, Object> map : lstWin) {
                    int rate = (Integer) map.get("rate");
                    money = _dbController.getMoneyBettingOfPlayer(_GameID, winner.getUserName(), secondYacht);
                    bonus = (rate * money * Global.BONUS_MONEY_SECOND) / 100 + money;
                }
                _dbController.updateMoneyResult(_GameID, winner.getUserName(), secondYacht, bonus);
                _dbController.setMoney(winner.getUserName(), winner.getMoney(), bonus,
                        Global.SERVER_NAME, "win 2nd in game id: " + _GameID);
                _dbController.writeLogWin(_GameID, winner.getUserName(), 0, secondYacht, 0, bonus);
            }
        }
        if (_map.get(this.thirdYacht) != null) {
            LinkedList<Player> lstPlayer3rd = _map.get(this.thirdYacht).getListPlayerPlay();
            for (Player winner : lstPlayer3rd) {
                winner = _dbController.getPlayerData(winner.getUserName());
                List<Map<String, Object>> lstWin = _dbController.getListWinner(_GameID, thirdYacht);
                int bonus = 0;
                int money = 0;
                for (Map<String, Object> map : lstWin) {
                    int rate = (Integer) map.get("rate");
                    money = _dbController.getMoneyBettingOfPlayer(_GameID, winner.getUserName(), thirdYacht);
                    bonus = (rate * money * Global.BONUS_MONEY_THIRD) / 100 + money;
                }
                _dbController.updateMoneyResult(_GameID, winner.getUserName(), thirdYacht, bonus);
                _dbController.setMoney(winner.getUserName(), winner.getMoney(), bonus,
                        Global.SERVER_NAME, "win 3rd in game id: " + _GameID);
                _dbController.writeLogWin(_GameID, winner.getUserName(), 0, 0, thirdYacht, bonus);

            }
        }
    }

    public void buildHorseMap() {
        if (_lstPlayerPlaying == null) {
            return;
        }
        try {
            _map = new ConcurrentHashMap<Integer, BettingYacht>(Global.MAX_YACHT);
            for (int yachtId = 1; yachtId <= Global.MAX_YACHT; yachtId++) {
                BettingYacht tmpBet = new BettingYacht(yachtId);
                for (int j = 0; j < _lstPlayerPlaying.size(); j++) {
                    Player player = _lstPlayerPlaying.get(j);
                    if (player.getCountChoiceByYachtId(yachtId) > 0) {
                        tmpBet.addPlayerPlay(player);
                    }
                }
                _map.put(yachtId, tmpBet);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void lookupBestHorse() {
        List<Integer> lstNotDemo = _dbController.getTopBettingDESC(_GameID);

        List<Integer> lstNotBetting = lstIdNotBetting(lstNotDemo);
        //shuffle list yach not betting
        Collections.shuffle(lstNotBetting);
        if (lstNotDemo.size() == 10) {
            firstYacht = lstNotDemo.get(0);
            secondYacht = lstNotDemo.get(1);
            thirdYacht = lstNotDemo.get(2);
        } else if (lstNotDemo.size() == 9) {
            secondYacht = lstNotDemo.get(0);
            thirdYacht = lstNotDemo.get(1);
            firstYacht = lstNotBetting.get(0);
        } else if (lstNotDemo.size() == 8) {
            thirdYacht = lstNotDemo.get(0);
            firstYacht = lstNotBetting.get(0);
            secondYacht = lstNotBetting.get(1);
        } else {
            firstYacht = lstNotBetting.get(0);
            secondYacht = lstNotBetting.get(1);
            thirdYacht = lstNotBetting.get(2);
        }
    }

    private List<Integer> lstIdNotBetting(List<Integer> lst) {
        List<Integer> lstId = new ArrayList<Integer>();
        for (int i = 1; i <= 10; i++) {
            if (!lst.contains(i)) {
                lstId.add(i);
            }
        }
        return lstId;
    }

    public void addPlayerTrackRace(Player player) {
        _lstPlayerTrackRace.add(player);
    }

    public List<Player> getListPlayerTrackRace() {
        return _lstPlayerTrackRace;
    }

    public void removePlayerTrackRace(String username) {
        int index = -1;
        for (int i = 0; i < _lstPlayerTrackRace.size(); i++) {
            if (username.equals(_lstPlayerTrackRace.get(i).getUserName())) {
                index = i;
            }
        }
        if (index != -1 && !_lstPlayerTrackRace.isEmpty()) {
            _lstPlayerTrackRace.remove(index);
        }
    }

    public void reSetEndGame() {
        this.firstYacht = 0;
        this.secondYacht = 0;
        this.thirdYacht = 0;
        this._map = null;
        this._track = null;
        this._countWinner = 0;
        this._countSecondWin = 0;
        this._countThirdWin = 0;
        this._lstPlayerPlaying = null;
        this.arrRate = randomRate();
        this.setGameState(GameState.Waiting);
        this.setTimeCloseGame(_dbController.getTimeRemain());
        _GameID = _dbController.prepareStartGame();
//        if (pay != null) {
//            pay.stop();
//            pay = null;
//        }
//        System.gc();
    }

    private void broadcastGameState() {
        EsObject esMessage = new EsObject();
        esMessage.setString(Field.Command.getName(), Field.GameState.getName());
        esMessage.setInteger(Field.GameState.getName(), this.getGameState().getState());
        _api.getLogger().warn(esMessage.toString());
        _api.sendGlobalPluginMessage(esMessage);
    }

    public void addPlayer(Player newPlayer) {
    }

    public GameState getGameState() {
        return _gameState;
    }

    public void setGameState(GameState gameState) {
        this._gameState = gameState;
    }

    public long getTimeNextGame() {
        return timeNextGame - System.currentTimeMillis();
    }

    public void setTimeCloseGame(long timeCloseGame_) {
        this.timeNextGame = timeCloseGame_;
    }

    public int getGameID() {
        return _GameID;
    }

    public void setGameID(int _GameID) {
        this._GameID = _GameID;
    }

    public boolean isMapNotNull() {
        if (_map != null) {
            if (!_map.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int[] randomRate() {
        int rdRate[] = new int[10];
        for (int i = 0; i < 10; i++) {
            int rd = RandomUtil.getNumMaxRandom(1, 100);
            int rdn = 0;
            if (rd >= 85) {
                rdn = RandomUtil.getNumMaxRandom(8, 10);
            } else if (rd < 85 && rd > 50) {
                rdn = RandomUtil.getNumMaxRandom(5, 7);
            } else {
                rdn = RandomUtil.getNumMaxRandom(1, 4);
            }
            rdRate[i] = rdn;
        }
        return rdRate;

    }

    public int[] getArrRate() {
        return arrRate;
    }

    public void setArrRate(int[] arrRate) {
        this.arrRate = arrRate;
    }
}
