package org.bouncycastle.jcajce;

import java.security.cert.CertPathParameters;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.PKIXParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.asn1.x509.GeneralName;

/**
 * This class extends the PKIXParameters with a validity model parameter.
 */
public class PKIXExtendedParameters
    implements CertPathParameters
{
    /**
     * This is the default PKIX validity model. Actually there are two variants
     * of this: The PKIX model and the modified PKIX model. The PKIX model
     * verifies that all involved certificates must have been valid at the
     * current time. The modified PKIX model verifies that all involved
     * certificates were valid at the signing time. Both are indirectly choosen
     * with the {@link java.security.cert.PKIXParameters#setDate(java.util.Date)} method, so this
     * methods sets the Date when <em>all</em> certificates must have been
     * valid.
     */
    public static final int PKIX_VALIDITY_MODEL = 0;

    /**
     * This model uses the following validity model. Each certificate must have
     * been valid at the moment where is was used. That means the end
     * certificate must have been valid at the time the signature was done. The
     * CA certificate which signed the end certificate must have been valid,
     * when the end certificate was signed. The CA (or Root CA) certificate must
     * have been valid, when the CA certificate was signed and so on. So the
     * {@link java.security.cert.PKIXParameters#setDate(java.util.Date)} method sets the time, when
     * the <em>end certificate</em> must have been valid. <p/> It is used e.g.
     * in the German signature law.
     */
    public static final int CHAIN_VALIDITY_MODEL = 1;

    public static class Builder
    {
        private final PKIXParameters baseParameters;
        private final Date date;

        private PKIXCertStoreSelector targetConstraints;
        private List extraCertStores = new ArrayList();
        private Map namedCertificateStoreMap = new HashMap();
        private List extraCRLStores = new ArrayList();
        private Map namedCRLStoreMap = new HashMap();
        private boolean revocationEnabled;
        private int validityModel = PKIX_VALIDITY_MODEL;
        private boolean useDeltas = false;

        public Builder(PKIXParameters baseParameters)
        {
            this.baseParameters = (PKIXParameters)baseParameters.clone();
            CertSelector constraints = baseParameters.getTargetCertConstraints();
            if (constraints != null)
            {
                this.targetConstraints = new PKIXCertStoreSelector.Builder(constraints).build();
            }
            Date checkDate = baseParameters.getDate();
            this.date = (checkDate == null) ? new Date() : checkDate;
            this.revocationEnabled = baseParameters.isRevocationEnabled();
        }

        public Builder(PKIXExtendedParameters baseParameters)
        {
            this.baseParameters = baseParameters.baseParameters;
            this.date = baseParameters.date;
            this.targetConstraints = baseParameters.targetConstraints;
            this.extraCertStores = new ArrayList(baseParameters.extraCertStores);
            this.namedCertificateStoreMap = new HashMap(baseParameters.namedCertificateStoreMap);
            this.extraCRLStores = new ArrayList(baseParameters.extraCRLStores);
            this.namedCRLStoreMap = new HashMap(baseParameters.namedCRLStoreMap);
            this.useDeltas = baseParameters.useDeltas;
            this.validityModel = baseParameters.validityModel;
            this.revocationEnabled = baseParameters.isRevocationEnabled();
        }

        public Builder addCertificateStore(PKIXCertStore store)
        {
            extraCertStores.add(store);

            return this;
        }

        public Builder addNamedCertificateStore(GeneralName issuerAltName, PKIXCertStore store)
        {
            namedCertificateStoreMap.put(issuerAltName, store);

            return this;
        }

        public Builder addCRLStore(PKIXCRLStore store)
        {
            extraCRLStores.add(store);

            return this;
        }

        public Builder addNamedCRLStore(GeneralName issuerAltName, PKIXCRLStore store)
        {
            namedCRLStoreMap.put(issuerAltName, store);

            return this;
        }

        public Builder setTargetConstraints(PKIXCertStoreSelector selector)
        {
            targetConstraints = selector;

            return this;
        }

        /**
         * Sets if delta CRLs should be used for checking the revocation status.
         *
         * @param useDeltas <code>true</code> if delta CRLs should be used.
         */
        public Builder setUseDeltasEnabled(boolean useDeltas)
        {
            this.useDeltas = useDeltas;

            return this;
        }

        /**
         * @param validityModel The validity model to set.
         * @see #CHAIN_VALIDITY_MODEL
         * @see #PKIX_VALIDITY_MODEL
         */
        public Builder setValidityModel(int validityModel)
        {
            this.validityModel = validityModel;

            return this;
        }

        /**
         * Flag whether or not revocation checking is to be enabled.
         *
         * @param revocationEnabled  true if revocation checking to be enabled, false otherwise.
         */
        public void setRevocationEnabled(boolean revocationEnabled)
        {
            this.revocationEnabled = revocationEnabled;
        }

        public PKIXExtendedParameters build()
        {
            return new PKIXExtendedParameters(this);
        }
    }

    private final PKIXParameters baseParameters;
    private final PKIXCertStoreSelector targetConstraints;
    private final Date date;
    private final List extraCertStores;
    private final Map namedCertificateStoreMap;
    private final List extraCRLStores;
    private final Map namedCRLStoreMap;
    private final boolean revocationEnabled;
    private final boolean useDeltas;
    private final int validityModel;

    private PKIXExtendedParameters(Builder builder)
    {
        this.baseParameters = builder.baseParameters;
        this.date = builder.date;
        this.extraCertStores = Collections.unmodifiableList(builder.extraCertStores);
        this.namedCertificateStoreMap = Collections.unmodifiableMap(new HashMap(builder.namedCertificateStoreMap));
        this.extraCRLStores = Collections.unmodifiableList(builder.extraCRLStores);
        this.namedCRLStoreMap = Collections.unmodifiableMap(new HashMap(builder.namedCRLStoreMap));
        this.targetConstraints = builder.targetConstraints;
        this.revocationEnabled = builder.revocationEnabled;
        this.useDeltas = builder.useDeltas;
        this.validityModel = builder.validityModel;
    }

    public List getCertificateStores()
    {
        return extraCertStores;
    }


    public Map getNamedCertificateStoreMap()
    {
        return namedCertificateStoreMap;
    }

    public List getCRLStores()
    {
        return extraCRLStores;
    }

    public Map getNamedCRLStoreMap()
    {
        return namedCRLStoreMap;
    }

    public Date getDate()
    {
        return new Date(date.getTime());
    }




    /**
     * Defaults to <code>false</code>.
     *
     * @return Returns if delta CRLs should be used.
     */
    public boolean isUseDeltasEnabled()
    {
        return useDeltas;
    }



    /**
     * @return Returns the validity model.
     * @see #CHAIN_VALIDITY_MODEL
     * @see #PKIX_VALIDITY_MODEL
     */
    public int getValidityModel()
    {
        return validityModel;
    }

    public Object clone()
    {
        return this;
    }

    /**
     * Returns the required constraints on the target certificate.
     * The constraints are returned as an instance of
     * <code>Selector</code>. If <code>null</code>, no constraints are
     * defined.
     *
     * @return a <code>Selector</code> specifying the constraints on the
     *         target certificate or attribute certificate (or <code>null</code>)
     * @see org.bouncycastle.jcajce.PKIXCertStoreSelector
     */
    public PKIXCertStoreSelector getTargetConstraints()
    {
        return targetConstraints;
    }

    public Set getTrustAnchors()
    {
        return baseParameters.getTrustAnchors();
    }

    public Set getInitialPolicies()
    {
        return baseParameters.getInitialPolicies();
    }

    public String getSigProvider()
    {
        return baseParameters.getSigProvider();
    }

    public boolean isExplicitPolicyRequired()
    {
        return baseParameters.isExplicitPolicyRequired();
    }

    public boolean isAnyPolicyInhibited()
    {
        return baseParameters.isAnyPolicyInhibited();
    }

    public boolean isPolicyMappingInhibited()
    {
        return baseParameters.isPolicyMappingInhibited();
    }

    public List getCertPathCheckers()
    {
        return baseParameters.getCertPathCheckers();
    }

    public List getCertStores()
    {
        return baseParameters.getCertStores();
    }

    public boolean isRevocationEnabled()
    {
        return revocationEnabled;
    }

}
