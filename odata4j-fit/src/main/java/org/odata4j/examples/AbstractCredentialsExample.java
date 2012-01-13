package org.odata4j.examples;

public abstract class AbstractCredentialsExample extends AbstractExample {

  private String loginName;
  private String loginPassword;

  protected String getLoginName() {
    return loginName;
  }

  protected void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  protected String getLoginPassword() {
    return loginPassword;
  }

  protected void setLoginPassword(String loginPassword) {
    this.loginPassword = loginPassword;
  }

}
