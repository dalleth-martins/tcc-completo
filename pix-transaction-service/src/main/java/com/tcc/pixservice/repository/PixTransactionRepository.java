package com.tcc.pixservice.repository;

import com.tcc.pixservice.entidade.PixTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PixTransactionRepository extends MongoRepository<PixTransaction, String> {

    Optional<PixTransaction> findByTransactionId(String transactionId);

    Optional<PixTransaction> findByIdempotencyKey(String idempotencyKey);
}
