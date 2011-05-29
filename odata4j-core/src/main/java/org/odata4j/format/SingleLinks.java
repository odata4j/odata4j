package org.odata4j.format;

public class SingleLinks {

  private SingleLinks() {}
  
  public static SingleLink create(String uri) {
    return new SingleLinkImpl(uri);
  }
  
  private static class SingleLinkImpl implements SingleLink {

    private final String uri;
    
    public SingleLinkImpl(String uri) {
      this.uri = uri;
    }
    @Override
    public String getUri() {
      return uri;
    }
    
    @Override
    public String toString() {
      return String.format("SingleLink[%s]",uri);
    }
  }
}
