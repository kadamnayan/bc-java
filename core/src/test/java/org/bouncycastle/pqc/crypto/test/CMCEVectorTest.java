package org.bouncycastle.pqc.crypto.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import junit.framework.TestCase;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.SecretWithEncapsulation;
import org.bouncycastle.pqc.crypto.cmce.CMCEKEMExtractor;
import org.bouncycastle.pqc.crypto.cmce.CMCEKEMGenerator;
import org.bouncycastle.pqc.crypto.cmce.CMCEKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEKeyPairGenerator;
import org.bouncycastle.pqc.crypto.cmce.CMCEParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.cmce.CMCEPublicKeyParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class CMCEVectorTest
    extends TestCase
{
//    public void testVectors()
//        throws Exception
    public static void main(String[] args)
    throws Exception
    {
        String files = "3488-64-cmce.rsp 4608-96-cmce.rsp 6688-128-cmce.rsp 6960-119-cmce.rsp 8192-128-cmce.rsp";
        files = "3488-64-f-cmce.rsp 4608-96-f-cmce.rsp 6688-128-f-cmce.rsp 6960-119-f-cmce.rsp 8192-128-f-cmce.rsp";
        int[][] polys = {
                {3, 1, 0},
                {10, 9, 6, 0},
                {7, 2, 1, 0},
                {8, 0},
                {7, 2, 1, 0}
        };
//        files = "6960-119-cmce.rsp";// 8192-128-cmce.rsp";
//        files = "8192-128-cmce.rsp";
//        String files = "4608-96-cmce.rsp";// 6688-128-cmce.rsp 6960-119-cmce.rsp 8192-128-cmce.rsp";
        int fileIndex = 0;
        for (String name : files.split(" "))
        {
            System.out.println("testing: " + name);
            InputStream src = SphincsPlusTest.class.getResourceAsStream("/org/bouncycastle/pqc/crypto/test/cmce/" + name);
            BufferedReader bin = new BufferedReader(new InputStreamReader(src));

            String line = null;
            HashMap<String, String> buf = new HashMap<String, String>();
            while ((line = bin.readLine()) != null)
            {
                line = line.trim();

                if (line.startsWith("#"))
                {
                    continue;
                }
                if (line.length() == 0)
                {
                    if (buf.size() > 0)
                    {
                        String count = buf.get("count");
                        System.out.println("test case: " + count);

                        byte[] seed = Hex.decode(buf.get("seed")); // seed for cmce secure random
                        byte[] pk = Hex.decode(buf.get("pk"));     // public key
                        byte[] sk = Hex.decode(buf.get("sk"));     // private key
                        byte[] ct = Hex.decode(buf.get("ct"));     // ciphertext
                        byte[] ss = Hex.decode(buf.get("ss"));     // session key

                        String[] nameParts = name.split("-");
                        int m = 12;
                        int n = Integer.parseInt(nameParts[0]);
                        int t = Integer.parseInt(nameParts[1]);
                        boolean usingPivots = nameParts[2].equals("f");

                        if (n > 3488)
                        {
                            m = 13;
                        }

                        NISTSecureRandom random = new NISTSecureRandom(seed, null);
                        CMCEParameters parameters = new CMCEParameters(m, n, t, polys[fileIndex], usingPivots);

                        CMCEKeyPairGenerator kpGen = new CMCEKeyPairGenerator();
                        CMCEKeyGenerationParameters genParam = new CMCEKeyGenerationParameters(random, parameters);
                        //
                        // Generate keys and test.
                        //
                        kpGen.init(genParam);
                        AsymmetricCipherKeyPair kp = kpGen.generateKeyPair();

                        CMCEPublicKeyParameters pubParams = (CMCEPublicKeyParameters) kp.getPublic();
                        CMCEPrivateKeyParameters privParams = (CMCEPrivateKeyParameters) kp.getPrivate();

                        assertTrue(name + " " + count + ": public key", Arrays.areEqual(pk, pubParams.getPublicKey()));
                        assertTrue(name + " " + count + ": secret key", Arrays.areEqual(sk, privParams.getPrivateKey()));
                        
                        // KEM Enc
                        CMCEKEMGenerator cmceEncCipher = new CMCEKEMGenerator(random);
                        SecretWithEncapsulation secWenc = cmceEncCipher.generateEncapsulated(pubParams);
                        byte[] generated_cipher_text = secWenc.getEncapsulation();
                        assertTrue(name + " " + count + ": kem_enc cipher text", Arrays.areEqual(ct, generated_cipher_text));
                        assertTrue(name + " " + count + ": kem_enc key", Arrays.areEqual(ss, secWenc.getSecret()));

                        // KEM Dec
                        CMCEKEMExtractor cmceDecCipher = new CMCEKEMExtractor(privParams);

                        byte[] dec_key = cmceDecCipher.extractSecret(generated_cipher_text);

                        assertTrue(name + " " + count + ": kem_dec ss", Arrays.areEqual(dec_key, ss));
                        assertTrue(name + " " + count + ": kem_dec key", Arrays.areEqual(dec_key, secWenc.getSecret()));

                    }
                    buf.clear();

                    continue;
                }

                int a = line.indexOf("=");
                if (a > -1)
                {
                    buf.put(line.substring(0, a).trim(), line.substring(a + 1).trim());
                }


            }
            System.out.println("testing successful!");

            fileIndex++;
        }

    }
}