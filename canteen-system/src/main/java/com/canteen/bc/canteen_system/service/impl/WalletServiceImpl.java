package com.canteen.bc.canteen_system.service.impl;

import com.canteen.bc.canteen_system.dto.WalletResponse;
import com.canteen.bc.canteen_system.dto.WalletTransactionDTO;
import com.canteen.bc.canteen_system.entity.OrderEntity;
import com.canteen.bc.canteen_system.entity.TrxEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.entity.WalletEntity;
import com.canteen.bc.canteen_system.exception.InsufficientFundsException;
import com.canteen.bc.canteen_system.mapper.DtoMapper;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.TrxStatus;
import com.canteen.bc.canteen_system.model.TrxType;
import com.canteen.bc.canteen_system.repository.OrderRepository;
import com.canteen.bc.canteen_system.repository.TrxRepository;
import com.canteen.bc.canteen_system.repository.UserRepository;
import com.canteen.bc.canteen_system.repository.WalletRepository;
import com.canteen.bc.canteen_system.service.WalletService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
  private final WalletRepository walletRepository;
  private final TrxRepository trxRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final DtoMapper dtoMapper;

  @Override
  public WalletResponse getWallet(Long userId) {
    WalletEntity wallet = walletRepository.findByUserId(userId).orElse(null);
    if (wallet != null) {
      return this.dtoMapper.map(wallet);
    }

    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return WalletResponse.builder()
        .id(null)
        .userId(user.getId())
        .userName(user.getName())
        .balance(BigDecimal.ZERO)
        .updatedAt(null)
        .build();
  }

  @Override
  public BigDecimal getBalance(Long userId) {
    return walletRepository
        .findByUserId(userId)
        .map(WalletEntity::getBalance)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  @Transactional
  public WalletTransactionDTO topUpWallet(
      Long userId, BigDecimal amount, PaymentMethod paymentMethod, String referenceId) {
    if (paymentMethod == null
        || !(paymentMethod == PaymentMethod.CASH || paymentMethod == PaymentMethod.CREDIT_CARD)) {
      throw new IllegalArgumentException("Wallet top-up must use CASH or CREDIT_CARD");
    }

    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    WalletEntity wallet =
        walletRepository
            .findByUserId(userId)
            .orElseGet(() -> WalletEntity.builder().user(user).balance(BigDecimal.ZERO).build());

    BigDecimal updatedBalance = wallet.getBalance().add(amount);
    wallet.setBalance(updatedBalance);
    walletRepository.save(wallet);

    TrxEntity transaction =
        TrxEntity.builder()
            .user(user)
            .transactionType(TrxType.TOP_UP)
            .paymentMethod(paymentMethod)
            .amount(amount)
            .status(TrxStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();

    TrxEntity saved = trxRepository.save(transaction);
    return this.dtoMapper.map(saved, updatedBalance);
  }

  @Override
  @Transactional
  public void debitWallet(Long userId, BigDecimal amount, Long orderId) {
    WalletEntity wallet =
        walletRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new InsufficientFundsException("No wallet found for user: " + userId));

    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new InsufficientFundsException("Insufficient wallet balance.");
    }

    BigDecimal updatedBalance = wallet.getBalance().subtract(amount);
    wallet.setBalance(updatedBalance);
    walletRepository.save(wallet);

    OrderEntity order = orderId != null ? orderRepository.findById(orderId).orElse(null) : null;
    TrxEntity trx = TrxEntity.builder()
        .user(wallet.getUser())
        .order(order)
        .transactionType(TrxType.PURCHASE)
        .paymentMethod(PaymentMethod.WALLET)
        .amount(amount)
        .status(TrxStatus.COMPLETED)
        .createdAt(LocalDateTime.now())
        .build();
    trxRepository.save(trx);
  }

  @Override
  @Transactional
  public void logCashPurchase(Long userId, BigDecimal amount, Long orderId) {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    OrderEntity order = orderId != null ? orderRepository.findById(orderId).orElse(null) : null;
    TrxEntity trx = TrxEntity.builder()
        .user(user)
        .order(order)
        .transactionType(TrxType.PURCHASE)
        .paymentMethod(PaymentMethod.CASH)
        .amount(amount)
        .status(TrxStatus.COMPLETED)
        .createdAt(LocalDateTime.now())
        .build();
    trxRepository.save(trx);
  }

  @Override
  @Transactional
  public void logManualCounterCashRefund(Long orderId, Long adminId) {
    OrderEntity order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

    BigDecimal refundAmount =
        order.getItems().stream()
            .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    TrxEntity refundTransaction =
        TrxEntity.builder()
            .user(order.getUser())
            .order(order)
            .transactionType(TrxType.REFUND)
            .paymentMethod(PaymentMethod.CASH)
            .amount(refundAmount)
            .status(TrxStatus.COMPLETED)
            .createdAt(LocalDateTime.now())
            .build();

    trxRepository.save(refundTransaction);
  }

  @Override
  @Transactional
  public WalletTransactionDTO confirmCashTopUp(
      Long transactionId, boolean collected, Long adminId) {
    TrxEntity trx =
        trxRepository
            .findById(transactionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Transaction not found: " + transactionId));

    if (!TrxType.TOP_UP.equals(trx.getTransactionType())) {
      throw new IllegalArgumentException("Transaction is not a top-up: " + transactionId);
    }

    if (!PaymentMethod.CASH.equals(trx.getPaymentMethod())) {
      throw new IllegalArgumentException("Only CASH top-ups can be confirmed via this endpoint.");
    }

    if (!collected) {
      trx.setStatus(TrxStatus.FAILED);
      trxRepository.save(trx);
      BigDecimal balance =
          walletRepository
              .findByUserId(trx.getUser().getId())
              .map(WalletEntity::getBalance)
              .orElse(BigDecimal.ZERO);
      return this.dtoMapper.map(trx, balance);
    }

    WalletEntity wallet =
        walletRepository
            .findByUserId(trx.getUser().getId())
            .orElseGet(
                () -> WalletEntity.builder().user(trx.getUser()).balance(BigDecimal.ZERO).build());
    BigDecimal updatedBalance = wallet.getBalance().add(trx.getAmount());
    wallet.setBalance(updatedBalance);
    walletRepository.save(wallet);

    trx.setStatus(TrxStatus.COMPLETED);
    trxRepository.save(trx);
    return this.dtoMapper.map(trx, updatedBalance);
  }

  @Override
  public java.util.List<com.canteen.bc.canteen_system.dto.WalletTransactionDTO> getTransactionHistory(Long userId) {
    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    return trxRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(trx -> this.dtoMapper.map(trx, null))
        .toList();
  }

  @Override
  @Transactional
  public WalletTransactionDTO refundToWallet(Long orderId, Long adminId) {
    OrderEntity order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

    BigDecimal refundAmount = order.getItems().stream()
        .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    WalletEntity wallet = walletRepository.findByUserId(order.getUser().getId())
        .orElseGet(() -> WalletEntity.builder()
            .user(order.getUser()).balance(BigDecimal.ZERO).build());

    BigDecimal updatedBalance = wallet.getBalance().add(refundAmount);
    wallet.setBalance(updatedBalance);
    walletRepository.save(wallet);

    TrxEntity refundTrx = TrxEntity.builder()
        .user(order.getUser())
        .order(order)
        .transactionType(TrxType.REFUND)
        .paymentMethod(PaymentMethod.WALLET)
        .amount(refundAmount)
        .status(TrxStatus.COMPLETED)
        .createdAt(LocalDateTime.now())
        .build();

    TrxEntity saved = trxRepository.save(refundTrx);
    return this.dtoMapper.map(saved, updatedBalance);
  }

  @Override
  @Transactional
  public WalletTransactionDTO processCardWebhook(
      Long transactionId, boolean success, String gatewayReference) {
    TrxEntity trx =
        trxRepository
            .findById(transactionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Transaction not found: " + transactionId));

    if (!TrxType.TOP_UP.equals(trx.getTransactionType())) {
      throw new IllegalArgumentException("Transaction is not a top-up: " + transactionId);
    }

    if (!PaymentMethod.CREDIT_CARD.equals(trx.getPaymentMethod())) {
      throw new IllegalArgumentException("Only CREDIT_CARD top-ups are processed via webhook.");
    }

    if (!success) {
      trx.setStatus(TrxStatus.FAILED);
      trxRepository.save(trx);
      BigDecimal balance =
          walletRepository
              .findByUserId(trx.getUser().getId())
              .map(WalletEntity::getBalance)
              .orElse(BigDecimal.ZERO);
      return this.dtoMapper.map(trx, balance);
    }

    WalletEntity wallet =
        walletRepository
            .findByUserId(trx.getUser().getId())
            .orElseGet(
                () -> WalletEntity.builder().user(trx.getUser()).balance(BigDecimal.ZERO).build());
    BigDecimal updatedBalance = wallet.getBalance().add(trx.getAmount());
    wallet.setBalance(updatedBalance);
    walletRepository.save(wallet);

    trx.setStatus(TrxStatus.COMPLETED);
    trxRepository.save(trx);
    return this.dtoMapper.map(trx, updatedBalance);
  }
}
