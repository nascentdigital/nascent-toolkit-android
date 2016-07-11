package com.nascentdigital.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Utility class supporting data encryption and decryption.
 */
public final class EncryptionHelper
{
    // [region] constants



    // [endregion]


    // [region] instance variables

    private AesCbcWithIntegrity.SecretKeys mSecretKeys;

    // [endregion]


    // [region] constructors

    /**
     * Constructs a new instance of the encryption helper.
     *
     */
    public  EncryptionHelper(Context context)
    {
        initialize(context);
    }

    private synchronized void initialize(Context context)
    {
        // initialize instance variables
        try
        {
            SharedPreferences sharedPref = context.getSharedPreferences("com.telus.telusmyaccount", Context.MODE_PRIVATE);
            String keysString = sharedPref.getString("com.telus.telusmyaccount.keys", null);

            //If no prior key string saved, create a new one and save it to stored preferences
            if (keysString == null) {
                mSecretKeys = AesCbcWithIntegrity.generateKey();
                keysString = AesCbcWithIntegrity.keyString(mSecretKeys);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("com.telus.telusmyaccount.keys", keysString);
                editor.commit();
            }
            else
            {
                mSecretKeys = AesCbcWithIntegrity.keys(keysString);
            }


        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    // [endregion]


    // [region] public methods



    /**
     * Encrypts the specified string.
     *
     * @param value
     *            decrypted string to be encrypted.
     */
    public String encrypt(String value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return AesCbcWithIntegrity.encrypt(value, mSecretKeys).toString();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts the specified string.
     *
     * @param value
     *            encrypted string to be decrypted.
     */
    public String decrypt(String value)
    {
        try
        {
            return AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(value), mSecretKeys);
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


   //endregion

} // class EncryptionHelper
