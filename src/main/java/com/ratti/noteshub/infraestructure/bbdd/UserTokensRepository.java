package com.ratti.noteshub.infraestructure.bbdd;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.ratti.noteshub.domain.entity.UserTokens;

public interface UserTokensRepository extends CrudRepository<UserTokens, Long> {
  Optional<UserTokens> findByTokenAndType(String token, byte type);

  Optional<UserTokens> findByUserIdAndType(long userId, byte type);
}
