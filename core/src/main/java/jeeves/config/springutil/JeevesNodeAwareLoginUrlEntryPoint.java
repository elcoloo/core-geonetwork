/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package jeeves.config.springutil;

import org.fao.geonet.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getApplicationContextFromServletContext;
import static jeeves.config.springutil.JeevesDelegatingFilterProxy.getServletContext;

/**
 * Created by Jesse on 2/17/14.
 */
public class JeevesNodeAwareLoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {
    @Autowired
    private ServletContext context;

    public JeevesNodeAwareLoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        final ServletContext context = getServletContext(this.context);
        final ConfigurableApplicationContext applicationContext = getApplicationContextFromServletContext(context);

        NodeInfo nodeInfo = applicationContext.getBean(NodeInfo.class);

        final String baseUrl = super.buildRedirectUrlToLoginPage(request, response, authException);
        String newUrl = baseUrl.replace("@@nodeId@@", nodeInfo.getId());

        return newUrl;
    }
}
