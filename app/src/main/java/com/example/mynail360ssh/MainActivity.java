package com.example.mynail360ssh;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etHost;
    private Button btnLogin;
    private TextView tvConsole;
    private Button btnClearConsole;
    private Button btnExecute;
    private JSch jsch;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etHost = findViewById(R.id.et_host);
        btnLogin = findViewById(R.id.btn_login);
        tvConsole = findViewById(R.id.tv_console);
        btnClearConsole = findViewById(R.id.btn_clear_console);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnLogin.getText().toString().equalsIgnoreCase("Login")) {
                    String username = etUsername.getText().toString();
                    String password = etPassword.getText().toString();
                    String host = etHost.getText().toString();

                    new SSHLoginTask().execute(username, password, host);
                }else{
                    session.disconnect();
                    etUsername.setVisibility(View.VISIBLE);
                    etPassword.setVisibility(View.VISIBLE);
                    etHost.setVisibility(View.VISIBLE);
                    btnClearConsole.setText("");
                    btnLogin.setText("Login");

                    btnExecute.setEnabled(false);
                }
            }
        });

        btnExecute = findViewById(R.id.btn_execute);
        btnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etCommand = findViewById(R.id.et_command);
                String command = etCommand.getText().toString();
                etCommand.setText("");
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etCommand.getWindowToken(), 0);
                String cmd = command;
                if(command.startsWith("sql=")){
                    cmd = command.replace("sql=", "~/sql-update.sh ");
                } else if (command.startsWith("backup-db")) {
                    cmd = "cd ~/backup && ./backup-db.sh";
                } else if (command.startsWith("build-test")) {
                    cmd="cd ~/lxerp-test; ./build-lexorInfo.sh ";
                } else if (command.startsWith("build-live")) {
                    cmd="cd ~/lxerp; ./build-lexorInfo.sh ";
                }else if (command.startsWith("restart-live")) {
                    cmd="~/glassfish5/bin/asadmin restart-domain domain1";
                }else if (command.startsWith("restart-test")) {
                    cmd="~/glassfish5/bin/asadmin restart-domain lexorcircle";
                }

                tvConsole.append("\n"+cmd +"\n-------------\n");
                new SSHExecuteTask().execute(cmd);
            }
        });
        btnExecute.setEnabled(false);
        btnClearConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvConsole.setText("");
            }
        });

        Gson gson = new Gson();
        Type type = new TypeToken<List<Server>>(){}.getType();
        List<Server> servers = gson.fromJson(loadJSONFromAsset(), type);

        List<String> serverNames = new ArrayList<>();
        for (Server server : servers) {
            serverNames.add(server.getHost());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, serverNames);
        Spinner spinner = findViewById(R.id.server_spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Server selectedServer = servers.get(position);
//                EditText usernameEditText = findViewById(R.id.username_edit_text);
//                EditText passwordEditText = findViewById(R.id.password_edit_text);
//                EditText hostEditText = findViewById(R.id.host_edit_text);
//                usernameEditText.setText(selectedServer.user);
//                passwordEditText.setText(selectedServer.pwd);
//                hostEditText.setText(selectedServer.host);
                etUsername.setText(selectedServer.getUser());
                etPassword.setText(selectedServer.getPwd());
                etHost.setText(selectedServer.getHost());
                tvConsole.setText(selectedServer.getCommand());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private class SSHLoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String host = params[2];

            try {
                jsch = new JSch();
                session = jsch.getSession(username, host, 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();


                return true;
            } catch (JSchException e) {
                Log.e("SSHLoginTask", "Error connecting to host", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                tvConsole.append("\nConnected to host.\n");
                // Hide the username, password, and host fields
                etUsername.setVisibility(View.GONE);
                etPassword.setVisibility(View.GONE);
                etHost.setVisibility(View.GONE);

                btnLogin.setText("Logout " + etHost.getText());

                btnExecute.setEnabled(true);
            } else {
                tvConsole.setText("Error connecting to host.\n");
            }
        }
    }

    private class SSHExecuteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String command = params[0];

            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);

                InputStream in = channel.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                channel.connect();

                byte[] buffer = new byte[1024];
                int read;

                while ((read = in.read(buffer)) > 0) {
                    baos.write(buffer, 0, read);
                }

                channel.disconnect();

                return baos.toString();
            } catch (JSchException | IOException e) {
                Log.e("SSHExecuteTask", "Error executing command", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                tvConsole.append(result);
            } else {
                tvConsole.append("Error executing command.\n");
            }
        }
    }
    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("servers.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}