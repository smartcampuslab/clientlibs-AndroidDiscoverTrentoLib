/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.dt.custom.data;

import eu.trentorise.smartcampus.storage.sync.service.SyncStorageService;

public class DTSyncStorageService extends SyncStorageService {
//	private static final int ACCOUNT_NOTIFICATION_ID = 1;
//	@SuppressWarnings("deprecation")
	@Override
	public void handleSecurityProblem(String appToken, String dbName) {
//        Intent i = new Intent("eu.trentorise.smartcampus.START");
//        i.setPackage(this.getPackageName());
//
////        DTHelper.getAccessProvider().invalidateToken(this, null);
//        try {
//			DTHelper.getAccessProvider().logout(this);
//		} catch (AACException e) {
//		}
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        
//        int icon = R.drawable.stat_notify_error;
//        CharSequence tickerText = getString(R.string.token_expired);
//        long when = System.currentTimeMillis();
//        CharSequence contentText = getString(R.string.token_required);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
//
//        Notification notification = new Notification(icon, tickerText, when);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.setLatestEventInfo(this, tickerText, contentText, contentIntent);
//        
//        //disable for now
//        mNotificationManager.notify(ACCOUNT_NOTIFICATION_ID, notification);
	}
}
