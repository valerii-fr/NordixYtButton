package dev.nordix.yt.domain.helpers

import com.jetbrains.rd.util.Date
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

object CertHelper {

    fun generateSelfSignedCertificate(): KeyStore {
        val keyPairGenerator = KeyPairGenerator
            .getInstance("RSA")
            .apply { initialize(2048) }
        val keyPair = keyPairGenerator.generateKeyPair()
        val issuer = X500Principal("CN=WssServer, OU=Development, O=Nordix, L=Randomville, ST=Bourgogne, C=France")
        val serialNumber = BigInteger.valueOf(System.currentTimeMillis())
        val notBefore = Date()
        val notAfter = Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000) // сертификат на 1 год
        val certBuilder = JcaX509v3CertificateBuilder(
            issuer,
            serialNumber,
            notBefore,
            notAfter,
            issuer,
            keyPair.public
        )

        val contentSigner = JcaContentSignerBuilder(ALGORITHM_ID).build(keyPair.private)
        val certificateHolder = certBuilder.build(contentSigner)
        val certificate = JcaX509CertificateConverter()
            .getCertificate(certificateHolder)
            .apply {
                verify(keyPair.public)
            }

        return KeyStore.getInstance("JKS").apply {
            load(null, null)
            setKeyEntry(
                KEY_ALIAS,
                keyPair.private,
                KEY_PASSWORD.toCharArray(),
                arrayOf<X509Certificate>(certificate)
            )
        }
    }

    internal const val STORE_PASSWORD = "SProcKBAnTIoUtHetIeRmoRo"
    internal const val KEY_PASSWORD = "oRTySifiFEreOnexpENdANdA"
    internal const val KEY_ALIAS = "wss"
    private const val ALGORITHM_ID = "SHA256withRSA"

}
