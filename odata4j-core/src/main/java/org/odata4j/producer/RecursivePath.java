package org.odata4j.producer;


/**
 * A recursive path has an associated depth limiter.
 *
 * The last component of the path is a number indicating the maximum depth
 * to recurse.  0 means do not limit the recursion.
 *
 * Examples:
 * Properties/0
 * SubTypes/1
 */
public class RecursivePath extends Path {

  private final int depth;

  public RecursivePath(Path path, int depth) {
    super(path);
    this.depth = depth;
  }

  public int getDepth() {
    return depth;
  }

  public boolean isUnlimited() {
    return depth <= 0;
  }

  public boolean isValidAtDepth(int d) {
    return isUnlimited() || d <= this.depth;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && obj instanceof RecursivePath && ((RecursivePath) obj).depth == this.depth;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + this.depth;
  }

}
