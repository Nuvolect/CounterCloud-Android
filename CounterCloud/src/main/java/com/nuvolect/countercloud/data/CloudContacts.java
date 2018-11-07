package com.nuvolect.countercloud.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Entity;
import android.provider.ContactsContract.RawContacts;

public class CloudContacts {

    public static Uri getContactUri(Context ctx, long contact_id){

        ContentResolver contentResolver = ctx.getContentResolver();

        String where = ContactsContract.Contacts._ID + " = ? ";
        String[] args = { contact_id+"" };

        Cursor c = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI, //.CONTENT_LOOKUP_URI,//.CONTENT_URI,
                new String[]{ // projection
                        ContactsContract.Contacts.LOOKUP_KEY,
                },
                where, // selection
                args,  // selection params
                null
                );

        Uri contactUri = null;

        if( c.moveToFirst())
            contactUri = Contacts.getLookupUri(
                    contact_id,
                    c.getString( 0 ) // lookup_key column
                    );
        c.close();

        return contactUri;
    }

    public static Uri getContactUriViaRawId(Context ctx, long rawContactId){

        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        Uri entityUri = Uri.withAppendedPath(rawContactUri, Entity.CONTENT_DIRECTORY);

        return entityUri;
    }
}
