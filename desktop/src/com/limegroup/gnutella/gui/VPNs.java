/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2018, FrostWire(R). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.limegroup.gnutella.gui;

import com.frostwire.bittorrent.BTEngine;
import com.frostwire.jlibtorrent.EnumNet;
import org.limewire.util.OSUtils;

import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 */
public final class VPNs {

    public static boolean isVPNActive() {
        boolean result = false;

        if (OSUtils.isMacOSX() || OSUtils.isLinux()) {
            result = isPosixVPNActive();
        } else if (OSUtils.isWindows()) {
            result = isWindowsVPNActive();
        }

        return result;
    }

    /**
     * <strong>VPN ON (Mac)</strong>
     * <pre>Internet:
     * Destination        Gateway            Flags        Refs      Use   Netif Expire
     * 0/1                10.81.10.5         UGSc            5        0   utun1
     * ...</pre>
     * <p>
     * <strong>VPN ON (Linux)</strong>
     * <pre>Kernel IP routing table
     * Destination     Gateway         Genmask         Flags   MSS Window  irtt Iface
     * 0.0.0.0         10.31.10.5      128.0.0.0       UG        0 0          0 tun0
     * ...</pre>
     *
     * @return true if it finds a line that starts with "0" and contains "tun" in the output of "netstat -nr"
     */
    private static boolean isPosixVPNActive() {
        boolean result = false;
        try {
            List<EnumNet.IpRoute> routes = EnumNet.enumRoutes(BTEngine.getInstance());
            for (EnumNet.IpRoute route : routes) {
                if (route.destination().toString().equals("0.0.0.0") && route.name().contains("tun")) {
                    result = true;
                    break;
                }
            }
        } catch (Throwable t) {
            result = false;
        }

        return result;
    }

    private static boolean isWindowsVPNActive() {
        try {
            List<EnumNet.IpInterface> interfaces = EnumNet.enumInterfaces(BTEngine.getInstance());
            List<EnumNet.IpRoute> routes = EnumNet.enumRoutes(BTEngine.getInstance());
            return isWindowsPIAActive(interfaces, routes) ||
                    isExpressVPNActive(interfaces) ||
                    isWindowsCactusVPNActive(interfaces, routes) ||
                    isWindowsNordVPNActive(interfaces, routes) ||
                    isWindowsAvgVPNActive(interfaces, routes);
        } catch (Throwable t2) {
            t2.printStackTrace();
            return false;
        }
    }

    private static boolean isWindowsPIAActive(List<EnumNet.IpInterface> interfaces, List<EnumNet.IpRoute> routes) {
        return isWindowsVPNAdapterActive(interfaces, routes, "TAP-Windows Adapter");
    }

    private static boolean isWindowsCactusVPNActive(List<EnumNet.IpInterface> interfaces, List<EnumNet.IpRoute> routes) {
        return isWindowsVPNAdapterActive(interfaces, routes, "CactusVPN");
    }

    private static boolean isWindowsNordVPNActive(List<EnumNet.IpInterface> interfaces, List<EnumNet.IpRoute> routes) {
        return isWindowsVPNAdapterActive(interfaces, routes, "TAP-NordVPN");
    }

    private static boolean isWindowsAvgVPNActive(List<EnumNet.IpInterface> interfaces, List<EnumNet.IpRoute> routes) {
        return isWindowsVPNAdapterActive(interfaces, routes, "AVG TAP");
    }

    private static boolean isExpressVPNActive(List<EnumNet.IpInterface> interfaces) {
        boolean expressVPNTapAdapterPresent = false;
        for (EnumNet.IpInterface iface : interfaces) {
            if (iface.description().contains("ExpressVPN Tap Adapter") && iface.preferred()) {
                expressVPNTapAdapterPresent = true;
                break;
            }
        }
        return expressVPNTapAdapterPresent;
    }

    private static boolean isWindowsVPNAdapterActive(List<EnumNet.IpInterface> interfaces,
                                                     List<EnumNet.IpRoute> routes, String description) {
        EnumNet.IpInterface adapter = null;
        for (EnumNet.IpInterface iface : interfaces) {
            if (iface.description().contains(description) && iface.preferred()) {
                adapter = iface;
                break;
            }
        }

        if (adapter == null) {
            return false;
        }

        for (EnumNet.IpRoute route : routes) {
            if (route.name().contains(adapter.name())) {
                return true;
            }
        }
        return false;
    }
}
