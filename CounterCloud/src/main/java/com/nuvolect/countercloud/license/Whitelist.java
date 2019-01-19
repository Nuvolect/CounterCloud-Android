/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.license;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class Whitelist {

	/**
	 * Check is user is on whitelist.
	 * @param ctx
	 * @return t/f
	 */
	public static String onWhitelist(Context ctx) {

        Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);

        Account[] myAccounts = AccountManager.get(ctx).getAccounts();//FIXME add permission check for getAccounts

        for (Account myAccount : myAccounts) {

            String account_email = myAccount.name.toLowerCase(Locale.US).trim();

            if (EMAIL_PATTERN.matcher(account_email).matches()){

                if( developers.contains( account_email))
                    return account_email;
            }
        }

		return "";
	}

	/** Build the set of whitelist emails, all must be lower case */
	private static Set<String> developers = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
	{
//		add("usere@email.com");
	}};

}
