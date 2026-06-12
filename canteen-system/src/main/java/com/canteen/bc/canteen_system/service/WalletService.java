package com.canteen.bc.canteen_system.service;

import com.canteen.bc.canteen_system.dto.WalletResponse;
import com.canteen.bc.canteen_system.dto.WalletTransactionDTO;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import java.math.BigDecimal;

public interface WalletService {

  /**
   * Looks up user's active virtual balance.
   *
   * @param userId Target user ID.
   * @return Active BigDecimal wallet balance.
   */
  BigDecimal getBalance(Long userId);

  WalletResponse getWallet(Long userId);

  /**
   * Processes third-party gateway approvals (FPS, Credit Card) to add funds to virtual profile.
   *
   * @param userId Target user ID.
   * @param amount Monetary amount to credit.
   * @param paymentGateway Refers to transaction source ("FPS", "CREDIT_CARD").
   * @return Updated transaction statement confirmation details.
   */
  WalletTransactionDTO topUpWallet(
      Long userId, BigDecimal amount, PaymentMethod paymentMethod, String referenceId);

  /**
   * Deducts funds for internal transactions when an app checkout is initialized. Throws
   * InsufficientFundsException if wallet is empty.
   *
   * @param userId Buying customer ID.
   * @param amount Order cost to subtract.
   * @param orderId Linked order record ID.
   */
  void debitWallet(Long userId, BigDecimal amount, Long orderId);

  void logCashPurchase(Long userId, BigDecimal amount, Long orderId);

  /**
   * Auditing Hook: Logs a manual cash layout reversal when an admin settles a user cancellation
   * over the counter, preventing app ledger inconsistencies.
   *
   * @param orderId Target cancelled order ID.
   * @param adminId Admin executing the physical payout.
   */
  void logManualCounterCashRefund(Long orderId, Long adminId);

  WalletTransactionDTO confirmCashTopUp(Long transactionId, boolean collected, Long adminId);

  WalletTransactionDTO processCardWebhook(
      Long transactionId, boolean success, String gatewayReference);

  java.util.List<WalletTransactionDTO> getTransactionHistory(Long userId);

  WalletTransactionDTO refundToWallet(Long orderId, Long adminId);
}
