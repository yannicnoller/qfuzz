import org.apache.ws.security.WSSecurityException;
import org.apache.wss4j.binding.wss10.PasswordString;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


public class SimplifiedUsernameTokenValidatorImpl {
	
    public static boolean unsafe_String_equals(String s1, Object s2) {
        if (s1 == s2) {
            return true;
        }
        if (s2 instanceof String) {
            String anotherString = (String)s2;
            int n = s1.length();
            if (n == anotherString.length()) {
                char v1[] = s1.toCharArray();
                char v2[] = anotherString.toCharArray();
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }


	/**
	 * Verify a UsernameToken containing a plaintext password.
	 */
	public static void verifyPlaintextPassword(PasswordString passwordType, String dBPassword)
			throws WSSecurityException {

//		if (!passwordType.getValue().equals(dBPassword)) {
		if (!unsafe_String_equals(passwordType.getValue(), dBPassword)) {	
			throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
		}
		passwordType.setValue(dBPassword);
	}

}
