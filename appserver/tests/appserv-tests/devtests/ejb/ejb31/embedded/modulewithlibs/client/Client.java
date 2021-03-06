/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.acme;

import org.glassfish.tests.ejb.sample.SimpleEjb;

import java.util.Map;
import java.util.HashMap;
import jakarta.ejb.*;
import jakarta.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary(appName + "ID");
    }

    private void test() {

        Map<String, Object> p = new HashMap<String, Object>();
        p.put(EJBContainer.MODULES, "sample");

        EJBContainer c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/sample/SimpleEjb");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

            stat.addStatus("EJB embedded with JPA", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded with JPA", stat.FAIL);
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        try {
            System.out.println("Creating another container without closing...");
            EJBContainer c0 = EJBContainer.createEJBContainer();
            if (c0 != null) {
                stat.addStatus("EJB embedded create container", stat.FAIL);
                System.out.println("Created another container without closing the current...");
            }
        } catch (EJBException e) { 
            System.out.println("Caught expected: " + e.getMessage());
            stat.addStatus("EJB embedded create container", stat.PASS);
        }

        System.out.println("Closing container ...");
        try {
            c.close();
            stat.addStatus("EJB embedded close container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded close container", stat.FAIL);
            System.out.println("ERROR Closing container:");
            e.printStackTrace();
        }
        System.out.println("Done Closing container");

        System.out.println("Creating container after closing the previous...");
        try {
            c = EJBContainer.createEJBContainer(p);
            c.close();
            stat.addStatus("EJB embedded create new container", stat.PASS);
        } catch (Exception e) {
            stat.addStatus("EJB embedded create new container", stat.FAIL);
            System.out.println("ERROR in the 2nd container:");
            e.printStackTrace();
        }

        System.out.println("..........FINISHED Embedded test");
    }
}
