package com.example.auth.core.authorization;

import com.example.auth.core.Clock;
import com.google.common.base.Optional;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenCreationVerifierImplTest {
  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  private TokenCreationVerifier verifier;

  private String code = "code";

  private String clientId = "clientId";
  private String secret = "the secret";
  @Mock
  private ClientAuthorizationRepository repository;

  private Date currentDate = new Date();

  private Clock clock = new StubClock(currentDate);

  @Before
  public void setUp() throws Exception {
    verifier = new TokenCreationVerifierImpl(repository, clock);
  }

  @Test
  public void verify() throws Exception {

    final Authorization authorizationRequest = new Authorization("type", clientId, "Code", "redirectURI", "userId");

    context.checking(new Expectations() {{
      oneOf(repository).findByCode(code);
      will(returnValue(Optional.of(authorizationRequest)));
      oneOf(repository).update(authorizationRequest);
    }});

    assertTrue(verifier.verify(code, clientId));
    assertFalse(authorizationRequest.isNotUsed());
  }

  @Test
  public void notVerifyRequestWhenNotExists() throws Exception {

    context.checking(new Expectations() {{
      oneOf(repository).findByCode(code);
      will(returnValue(Optional.absent()));
    }});

    assertFalse(verifier.verify(code, clientId));
  }

  @Test
  public void notVerifiedWhenOtherClientIdWasPassed() throws Exception {

    final Authorization authorizationRequest = new Authorization("type", "other_clientId", "Code", "redirectURI", "userId");

    context.checking(new Expectations() {{
      oneOf(repository).findByCode(code);
      will(returnValue(Optional.of(authorizationRequest)));
    }});

    assertFalse(verifier.verify(code, clientId));
  }

  @Test
  public void notVerifiedWhenAlreadyUsedAuthorization() throws Exception {

    final Authorization authorizationRequest = new Authorization("type", "other_clientId", "Code", "redirectURI", "userId");
    //already used
    authorizationRequest.usedOn(new Date());

    context.checking(new Expectations() {{
      oneOf(repository).findByCode(code);
      will(returnValue(Optional.of(authorizationRequest)));
    }});

    assertFalse(verifier.verify(code, clientId));
  }

}