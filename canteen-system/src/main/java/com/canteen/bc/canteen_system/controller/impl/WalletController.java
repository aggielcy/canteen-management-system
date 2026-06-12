package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.dto.TopUpRequest;
import com.canteen.bc.canteen_system.dto.WalletResponse;
import com.canteen.bc.canteen_system.dto.WalletTransactionDTO;
import com.canteen.bc.canteen_system.dto.request.RefundRequest;
import com.canteen.bc.canteen_system.service.WalletService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
  private final WalletService walletService;

  @GetMapping("/{userId}")
  public ResponseEntity<WalletResponse> getWallet(@PathVariable Long userId) {
    return ResponseEntity.ok(walletService.getWallet(userId));
  }

  @GetMapping("/{userId}/transactions")
  public ResponseEntity<List<WalletTransactionDTO>> getTransactionHistory(@PathVariable Long userId) {
    return ResponseEntity.ok(walletService.getTransactionHistory(userId));
  }

  @PostMapping("/top-up")
  public ResponseEntity<WalletTransactionDTO> topUp(@RequestBody TopUpRequest request) {
    WalletTransactionDTO transaction = walletService.topUpWallet(
        request.getUserId(), request.getAmount(),
        request.getPaymentMethod(), request.getReferenceId());
    return ResponseEntity.ok(transaction);
  }

  @PostMapping("/refund")
  public ResponseEntity<WalletTransactionDTO> refund(@RequestBody RefundRequest request) {
    return ResponseEntity.ok(walletService.refundToWallet(request.getOrderId(), request.getAdminId()));
  }
}
