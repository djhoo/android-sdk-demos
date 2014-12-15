package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.*;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

  private EditText nameInput;
  private Button joinButton;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);


    joinButton = (Button) findViewById(R.id.button);
    joinButton.setOnClickListener(this);
    nameInput = (EditText) findViewById(R.id.editText);

    String predefinedName =
        PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
    if (predefinedName != null) {
      nameInput.setText(predefinedName);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  @Override
  public void onClick(View v) {
    final String name = nameInput.getText().toString();
    if (name == null || name.trim().isEmpty()) {
      nameInput.setError("");
      return;
    }

    SharedPreferences spr = PreferenceManager.getDefaultSharedPreferences(this);
    spr.edit().putString("username", name).commit();

    AVQuery<AVUser> q = AVUser.getQuery();
    q.whereEqualTo("username", name);
    q.getFirstInBackground(new GetCallback<AVUser>() {
      @Override
      public void done(AVUser object, AVException e) {
        if (e != null) {
          toastExeption(e);
        } else {
          if (object == null) {
            final AVUser user = new AVUser();
            user.setUsername(name);
            user.setPassword(name);
            user.signUpInBackground(new SignUpCallback() {
              @Override
              public void done(AVException e) {
                if (e != null) {
                  toastExeption(e);
                } else {
                  loginSucceed();
                }
              }
            });
          } else {
            AVUser.logInInBackground(name, name, new LogInCallback<AVUser>() {
              @Override
              public void done(AVUser user, AVException e) {
                if (e != null) {
                  toastExeption(e);
                } else {
                  loginSucceed();
                }
              }
            });
          }
        }
      }
    });
  }

  public void loginSucceed() {
    AVUser user = AVUser.getCurrentUser();
    String selfId = user.getObjectId();
    List<String> peerIds = new LinkedList<String>();
    Session session = SessionManager.getInstance(selfId);
    session.setSignatureFactory(new KeepAliveSignatureFactory(AVOSCloud.applicationId, selfId));
    session.open(selfId, peerIds);
  }

  public void toastExeption(AVException e) {
    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
  }
}
