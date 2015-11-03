
/*********************************************************************************************************
 * Software License Agreement (BSD License)
 * 
 * Copyright 2014 Next Century Corporation. All rights reserved.   
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ***********************************************************************************************************/
/**
 * Application configuration file for WebXml plugin.
 */
webxml {
        //========================================
        // Session Timeout
        //========================================
        //
        // uncomment to set session timeout - Be sure to specify value as an Integer
        // sessionConfig.sessionTimeout = 30

        //========================================
        // Delegating Filter Chain
        //========================================
        //
        // Add a 'filter chain proxy' delegater as a Filter.  This will allow the application
        // to define a FilterChainProxy bean that can add additional filters, such as
        // an instance of org.springframework.security.web.FilterChainProxy.

        // Set to true to add a filter chain delegator.
        //filterChainProxyDelegator.add = true

        // The name of the delegate FilterChainProxy bean.  You must ensure you have added a bean
        // with this name that implements FilterChainProxy to
        // YOUR-APP/grails-app/conf/spring/resources.groovy.
        //filterChainProxyDelegator.targetBeanName = "filterChainProxyDelegate"

        // The URL pattern to which the filter will apply.  Usually set to '/*' to cover all URLs.
        //filterChainProxyDelegator.urlPattern = "/*"

        // Set to true to add Listeners
        //listener.add = true
        //listener.classNames = ["org.springframework.web.context.request.RequestContextListener"]

        //-------------------------------------------------
        // These settings usually do not need to be changed
        //-------------------------------------------------

        // The name of the delegating filter.
        //filterChainProxyDelegator.filterName = "filterChainProxyDelegator"

        // The delegating filter proxy class.
        //filterChainProxyDelegator.className = "org.springframework.web.filter.DelegatingFilterProxy"

        // ------------------------------------------------
        // Example for context aparameters
        // ------------------------------------------------
        // this example will create the following XML part
        // contextparams = [port: '6001']
        //
        //  <context-param>
        //        <param-name>port</param-name>
        //        <param-value>6001</param-value>
        //  </context-param>
}
