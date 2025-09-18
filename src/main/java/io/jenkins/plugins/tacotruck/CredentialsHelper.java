package io.jenkins.plugins.tacotruck;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import java.util.Collections;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class CredentialsHelper {
    private static final Logger LOGGER = Logger.getLogger(CredentialsHelper.class.getName());

    @CheckForNull
    protected static StringCredentials lookupApiTokenCredentials(@CheckForNull String credentialsId) {
        if (credentialsId == null) {
            return null;
        }

        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.get(), ACL.SYSTEM),
                CredentialsMatchers.withId(credentialsId));
    }

    protected static ListBoxModel doFillCredentialsIdItems(Item item, String credentialsId) {
        StandardListBoxModel result = new StandardListBoxModel();

        if (item == null || !item.hasPermission(Item.CONFIGURE)) {
            return result.includeCurrentValue(credentialsId);
        }

        return result.includeEmptyValue()
                .includeMatchingAs(
                        ACL.SYSTEM,
                        item,
                        StandardCredentials.class,
                        Collections.<DomainRequirement>emptyList(),
                        CredentialsMatchers.anyOf(
                                CredentialsMatchers.instanceOf(StringCredentials.class),
                                CredentialsMatchers.instanceOf(UsernamePasswordCredentials.class)))
                .includeCurrentValue(credentialsId);
    }
}
