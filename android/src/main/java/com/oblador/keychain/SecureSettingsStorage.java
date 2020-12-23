package com.oblador.keychain;

import android.provider.Settings;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.oblador.keychain.cipherStorage.CipherStorage;

public class SecureSettingsStorage {

  private final ReactApplicationContext context;

  public SecureSettingsStorage(@NonNull final ReactApplicationContext reactContext) {
    this.context = reactContext;
  }

  public void storeEncryptedEntry(String alias, CipherStorage.EncryptionResult encryptionResult) {

    final String keyForUsername = getKeyForUsername(alias);
    final String keyForPassword = getKeyForPassword(alias);
    final String keyForCipherStorage = getKeyForCipherStorage(alias);

    Settings.Secure.putString(context.getContentResolver(), keyForUsername, Base64.encodeToString(encryptionResult.username, Base64.DEFAULT));
    Settings.Secure.putString(context.getContentResolver(), keyForPassword, Base64.encodeToString(encryptionResult.password, Base64.DEFAULT));
    Settings.Secure.putString(context.getContentResolver(), keyForCipherStorage, encryptionResult.cipherName);

  }

  public PrefsStorage.ResultSet getEncryptedEntry(String alias) {

    byte[] bytesForUsername = getBytesForUsername(alias);
    byte[] bytesForPassword = getBytesForPassword(alias);
    String cipherStorageName = getCipherStorageName(alias);

    // in case of wrong password or username
    if (bytesForUsername == null || bytesForPassword == null) {
      return null;
    }

    if (cipherStorageName == null) {
      // If the CipherStorage name is not found, we assume it is because the entry was written by an older
      // version of this library. The older version used Facebook Conceal, so we default to that.
      cipherStorageName = KeychainModule.KnownCiphers.FB;
    }

    return new PrefsStorage.ResultSet(cipherStorageName, bytesForUsername, bytesForPassword);
  }

  @Nullable
  private byte[] getBytesForUsername(@NonNull final String service) {
    final String key = getKeyForUsername(service);

    return getBytes(key);
  }

  @Nullable
  private byte[] getBytesForPassword(@NonNull final String service) {
    String key = getKeyForPassword(service);
    return getBytes(key);
  }

  @Nullable
  private String getCipherStorageName(@NonNull final String service) {
    String key = getKeyForCipherStorage(service);
    return Settings.Secure.getString(context.getContentResolver(), key);
  }

  @Nullable
  private byte[] getBytes(@NonNull final String key) {

    String value = Settings.Secure.getString(context.getContentResolver(), key);

    if (value != null) {
      return Base64.decode(value, Base64.DEFAULT);
    }

    return null;
  }

  @NonNull
  public static String getKeyForUsername(@NonNull final String service) {
    return service + ":" + "u";
  }

  @NonNull
  public static String getKeyForPassword(@NonNull final String service) {
    return service + ":" + "p";
  }

  @NonNull
  public static String getKeyForCipherStorage(@NonNull final String service) {
    return service + ":" + "c";
  }

  public void removeEntry(String alias) {
    final String keyForUsername = getKeyForUsername(alias);
    final String keyForPassword = getKeyForPassword(alias);
    final String keyForCipherStorage = getKeyForCipherStorage(alias);

    Settings.Secure.putString(context.getContentResolver(), keyForUsername, null);
    Settings.Secure.putString(context.getContentResolver(), keyForPassword, null);
    Settings.Secure.putString(context.getContentResolver(), keyForCipherStorage, null);
  }
}
