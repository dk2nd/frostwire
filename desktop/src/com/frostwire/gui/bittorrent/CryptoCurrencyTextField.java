/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.gui.bittorrent;

import com.limegroup.gnutella.gui.LimeTextField;

import java.util.HashMap;
import java.util.Set;

public class CryptoCurrencyTextField extends LimeTextField {
    
    public static enum CurrencyURIPrefix {
        BITCOIN("bitcoin:"),
        LITECOIN("litecoin:"),
        DOGECOIN("dogecoin:");

        private final String prefix;
        
        private CurrencyURIPrefix(String p) {
            prefix  = p;
        }

        public String toString() {
            return prefix;
        }
    }

    private final String prefix;
    
    private final HashMap<String,String> firstValidCharsOnAddress;
    
    public CryptoCurrencyTextField(CurrencyURIPrefix p) {
        prefix = p.toString();
        
        firstValidCharsOnAddress = new HashMap<String,String>();
        initFirstValidChars();
    }

    private void initFirstValidChars() {
        if (prefix.equals("bitcoin:")) {
            firstValidCharsOnAddress.put("1","1");
            firstValidCharsOnAddress.put("3","3");
        } else if (prefix.equals("litecoin:")) {
            firstValidCharsOnAddress.put("L","L");
        } else if (prefix.equals("dogecoin:")) {
            firstValidCharsOnAddress.put("D","D");
        }
    }
    
    public boolean hasValidPrefixOrNoPrefix() {
        boolean hasPrefix = false;
        boolean hasValidPrefix = false;
        String text = getText();
        
        if (text.contains(":")) {
            hasPrefix = true;
            hasValidPrefix = text.startsWith(prefix);
        } else {
            hasPrefix = false;
        }
        
        return (hasPrefix && hasValidPrefix) || !hasPrefix;
    }
    
    public boolean hasValidAddress() {
        boolean result = false;
        String text = getText().trim();
        if (text != null && !text.isEmpty()) {
            text = text.replaceAll(prefix, "");
            result = (26 <= text.length() && text.length() <= 34) && isFirstCharValid();
        }
        return result;
    }
    
    //To be invoked only after hasValidAddress() has returned true.
    public String normalizeValidAddress() {
        String result = getText().trim();
        if (!result.startsWith(prefix)) {
            result = prefix + result;
        }
        return result;
    }

    private boolean isFirstCharValid() {
        boolean foundChar = false;
        String text = getText().trim();
        if (text != null && !text.isEmpty()) {
            text = text.replaceAll(prefix, "");
            if (text.length() > 1) {
                String firstChar = text.substring(0, 1);
                Set<String> firstChars = firstValidCharsOnAddress.keySet();
                for (String key : firstChars) {
                    if (key.equals(firstChar)) {
                        foundChar = true;
                        break;
                    }
                }
            }
        }
        
        return foundChar;
    }    
}