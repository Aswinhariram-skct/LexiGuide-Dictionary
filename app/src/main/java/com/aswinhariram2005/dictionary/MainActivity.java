package com.aswinhariram2005.dictionary;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.aswinhariram2005.dictionary.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        queue = Volley.newRequestQueue(this);
        binding.searchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    String val = textView.getText().toString();
                    if (!val.isEmpty()) {
                        binding.searchLay.setErrorEnabled(false);

                        getDef(val);

                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        binding.searchLay.setErrorEnabled(true);
                        binding.lay.setVisibility(View.GONE);
                        binding.animationView.setVisibility(View.VISIBLE);

                    }

                    return true;
                }
                return false;
            }
        });
    }

    private void getDef(String word) {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.show();

        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;
        Log.d("Response", "getDef: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject object = response.getJSONObject(0);

                            // Word
                            String wordVal = object.optString("word", "N/A");
                            binding.word.setText(wordVal);
                            Log.d("Response", "getDef: " + wordVal);

                            // Meanings
                            JSONArray meanings = object.getJSONArray("meanings");

                            String nounDef = "N/A", verbDef = "N/A", synonyms = "N/A", antonyms = "N/A";

                            for (int i = 0; i < meanings.length(); i++) {
                                JSONObject meaningObject = meanings.getJSONObject(i);
                                String partOfSpeech = meaningObject.optString("partOfSpeech", "");

                                if (partOfSpeech.equalsIgnoreCase("noun")) {
                                    JSONArray definitions = meaningObject.optJSONArray("definitions");
                                    if (definitions != null && definitions.length() > 0) {
                                        nounDef = definitions.getJSONObject(0).optString("definition", "N/A");
                                    }
                                    synonyms = meaningObject.optJSONArray("synonyms") != null ? meaningObject.optJSONArray("synonyms").toString() : "N/A";
                                    antonyms = meaningObject.optJSONArray("antonyms") != null ? meaningObject.optJSONArray("antonyms").toString() : "N/A";
                                } else if (partOfSpeech.equalsIgnoreCase("verb")) {
                                    JSONArray definitions = meaningObject.optJSONArray("definitions");
                                    if (definitions != null && definitions.length() > 0) {
                                        verbDef = definitions.getJSONObject(0).optString("definition", "N/A");
                                    }
                                }
                            }

                            // Hide Keyboard
                            View view = getCurrentFocus();
                            if (view != null) {
                                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                if (manager != null) {
                                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }

                            // Update UI
                            binding.lay.setVisibility(View.VISIBLE);
                            binding.animationView.setVisibility(View.GONE);
                            binding.nounDef.setText(nounDef);
                            binding.verbDef.setText(verbDef);
                            binding.meanTxt.setText(synonyms);
                            binding.oppTxt.setText(antonyms);

                        } catch (JSONException e) {
                            showError("Sorry, result not found");
                        } finally {
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError("Something went wrong");
                        dialog.dismiss();
                    }
                });

        queue.add(request);
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        binding.searchLay.setErrorEnabled(true);
        binding.lay.setVisibility(View.GONE);
        binding.animationView.setVisibility(View.VISIBLE);
    }

}