/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.netgame.lobby.enums;

import com.duduto.Global;

/**
 *
 * @author Dark
 */
public enum Message {
    UsernameSpace("Tên này không được chấp nhận.Vui lòng chọn tên khác."),
    TokenFaild("Lỗi Trong quá trình đăng nhập.Vui lòng đăng nhập lại"),
    DeviceToken("Cập nhật thành công"),
    LogXuEmpty("Bạn chưa đổi khoai lần nào."),
    LogEmpty("Bạn chưa đổi quà lần nào."),
    RegisterGift("Đăng ký đổi quà thành công. "),
    ProcessWaiting("Đang xử lý"),
    EmailEmpty("Vui lòng nhập email"),
    LogBettingEmpty("Bạn chưa đặt cược lần nào."),
    SystemError("Lỗi hệ thống. Vui lòng thử lại sau."),
    UpdateSuccess("Cập nhật thông tin thành công.Số xu của bạn được reset về 0."),
    UserNameExist("Tên tài khoản đã tồn tại.Vui lòng đăng nhập hoặc chọn tên khác"),
    CloneAccount("Bạn chỉ được chơi thử "+ Global.TOTAL_CLONE_REGISTER_ACCOUNT +" tài khoản.Vui lòng đăng nhập hoặc cập nhật thông tin để tiếp tục chơi"),
    IsDemo("Tài khoản bạn đang dùng là tài khoản dùng thử.Vui lòng cập nhật thông tin để tiến hành giao dịch"),
    LackKhoai("Bạn không đủ khoai.Vui lòng nạp thêm khoai vào tài khoản"),
    LackXu("Bạn không đủ xu để thực hiện giao dịch này."),
    TradeUnknown("Lỗi trong quá trình giao dịch.Vui lòng thử lại sau"),
    EmailExist("Email này đã được sử dụng cho tài khoản khác.Vui lòng sử dụng email khác"),
    TransactionExist("Mã giao dịch không hợp lệ."),
    UserNameEmpty("Tên Tài Khoản Chưa Có.Vui Lòng Nhập Tên Tài Khoản");
    
    private final String message;

    private Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
