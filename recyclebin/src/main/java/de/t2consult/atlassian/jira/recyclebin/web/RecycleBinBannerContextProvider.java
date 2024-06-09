package de.t2consult.atlassian.jira.recyclebin.web;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.web.ContextProvider;
import de.t2consult.atlassian.jira.recyclebin.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.*;

@Scanned
public class RecycleBinBannerContextProvider implements ContextProvider {

    private static Date ExpirationDate= DateUtils.date(2021,6,1);

    private static final Logger log = LoggerFactory.getLogger(RecycleBinBannerContextProvider.class);

    @Override
    public void init(Map<String, String> map) throws PluginParseException {
    }


    @Override
    public Map<String, Object> getContextMap(Map<String, Object> map) {

        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        if (user==null)
            return null;

        String bannerMessage="";
        String bannerMessageTitle="";

        Locale locale = ComponentAccessor.getLocaleManager().getLocaleFor(user);
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);

        if (isBuildPendingExpiration()){
            bannerMessageTitle="Recyclebin for Jira expiring soon!";
            bannerMessage="The AddOn Recyclebin for Jira is expiring on "+df.format(ExpirationDate);
        }

        if (isBuildExpired()){
            bannerMessageTitle="Recyclebin for Jira expired!";
            bannerMessage="The AddOn RecycleBin for Jira has expired on "+df.format(ExpirationDate);
        }

        bannerMessage+=". Please check the marketplace for an update or contact the vendor at info@t2consult.net";

        Map<String, Object> velocityParams = new HashMap<>();

        velocityParams.put("displayBanner", isBuildExpired() || isBuildPendingExpiration() );

        velocityParams.put("errorClass", isBuildExpired() ? "error" : "warning" );
        velocityParams.put("bannerMessage", bannerMessage);
        velocityParams.put("bannerMessageTitle", bannerMessageTitle);

        return velocityParams;
    }



    public static boolean isBuildPendingExpiration(){

        Date now = new Date();
        Calendar working = GregorianCalendar.getInstance();
        working.setTime(ExpirationDate);
        working.add(Calendar.DATE,-30);
        return working.getTime().before(now);
    }

    public static boolean isBuildExpired(){

        Date now = new Date();
        return ExpirationDate.before(now);
    }
}

