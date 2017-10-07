package de.mediathekview.mlib.tool;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Deprecated
public final class Hash {
  private final MessageDigest md;
  private byte[] bytes;

  public Hash() {
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm not found", e);
    }
  }

  public Hash(final byte[] b) {
    this();
    update(b);
  }

  public Hash(final byte[] b, final int off, final int len) {
    this();
    update(b, off, len);
  }

  public Hash(final ByteBuffer bb) {
    this();
    update(bb);
  }

  public Hash(final char c) {
    this();
    update(c);
  }

  public Hash(final double d) {
    this();
    update(d);
  }

  public Hash(final float f) {
    this();
    update(f);
  }

  public Hash(final int i) {
    this();
    update(i);
  }

  public Hash(final long l) {
    this();
    update(l);
  }

  public Hash(final short s) {
    this();
    update(s);
  }

  public Hash(final String s) {
    this();
    update(s);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof Hash)) {
      return false;
    }

    final Hash hash = (Hash) obj;
    return Arrays.equals(getBytes(), hash.getBytes());
  }

  public Hash finish() {
    bytes = md.digest();
    return this;
  }

  public byte[] getBytes() {
    ensureFinished();
    return bytes;
  }

  @Override
  public int hashCode() {
    final byte[] b = getBytes();
    return b[0] << 24 | b[1] << 16 | b[2] << 8 | b[3];
  }

  public void reset() {
    bytes = null;
  }

  public String toHexString() {
    ensureFinished();
    final StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (final byte b : bytes) {
      sb.append(Character.forDigit(b >> 8 & 0xf, 16));
      sb.append(Character.forDigit(b & 0xf, 16));
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toHexString();
  }

  public Hash update(final byte b) {
    checkNotFinished();
    md.update(b);
    return this;
  }

  public Hash update(final byte[] b) {
    return update(b, 0, b.length);
  }

  public Hash update(final byte[] b, final int off, final int len) {
    checkNotFinished();
    md.update(b, off, len);
    return this;
  }

  public Hash update(final ByteBuffer bb) {
    checkNotFinished();
    md.update(bb);
    return this;
  }

  public Hash update(final char c) {
    return update((short) c);
  }

  public Hash update(final double d) {
    return update(Double.doubleToRawLongBits(d));
  }

  public Hash update(final float f) {
    return update(Float.floatToRawIntBits(f));
  }

  public Hash update(final int i) {
    return update((short) (i >> 16)).update((short) i);
  }

  public Hash update(final long l) {
    return update((int) (l >> 32)).update((int) l);
  }

  public Hash update(final short s) {
    return update((byte) (s >> 8)).update((byte) s);
  }

  public Hash update(final String s) {
    if (s != null) {
      for (int i = 0; i < s.length(); i++) {
        update(s.charAt(i));
      }
    }
    return this;
  }

  private void checkNotFinished() {
    if (bytes != null) {
      throw new IllegalStateException("Hash must be reset before resuse");
    }
  }

  private void ensureFinished() {
    if (bytes == null) {
      finish();
    }
  }
}
