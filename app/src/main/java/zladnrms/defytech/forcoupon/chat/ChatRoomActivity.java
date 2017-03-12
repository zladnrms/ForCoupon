package zladnrms.defytech.forcoupon.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import zladnrms.defytech.forcoupon.R;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView rv_chatlist;
    private ArrayList<ChatInfo> chatlist;
    private ChatlistAdapter rv_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        rv_chatlist = (RecyclerView) findViewById(R.id.rv_chatroom);
        chatlist =new ArrayList<>();
        rv_adapter =new ChatlistAdapter(chatlist);
        LinearLayoutManager verticalLayoutmanager
                = new LinearLayoutManager(ChatRoomActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_chatlist.setLayoutManager(verticalLayoutmanager);
        rv_chatlist.setAdapter(rv_adapter);
    }

    public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.ViewHolder> {

        private List<ChatInfo> verticalList;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_chat_nickname;
            TextView tv_chat_content;
            TextView tv_chat_date;

            public ViewHolder(View view) {
                super(view);

                tv_chat_nickname = (TextView) view.findViewById(R.id.tv_chat_nickname);;
                tv_chat_content = (TextView) view.findViewById(R.id.tv_chat_content);;
                tv_chat_date = (TextView) view.findViewById(R.id.tv_chat_date);;
            }
        }

        public ChatlistAdapter(List<ChatInfo> verticalList) {
            this.verticalList = verticalList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_chatlist, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final int chatId = chatlist.get(position).getId();
            final String nickname = chatlist.get(position).getNickname();
            final String content = chatlist.get(position).getContent();
            final String date = chatlist.get(position).getDate();

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            if (holder.tv_chat_nickname != null) {
                holder.tv_chat_nickname.setText(content);
            }

            if (holder.tv_chat_content != null) {
                holder.tv_chat_content.setText(content);
            }

            if (holder.tv_chat_date != null) {
                holder.tv_chat_date.setText(date);
            }
        }

        @Override
        public int getItemCount() {
            return verticalList.size();
        }
    }

    class ChatInfo { // 채팅 정보 클래스

        private int id;
        private String nickname;
        private String content;
        private String date;

        public ChatInfo(int _id, String _nickname, String _content, String _date) {
            this.id = _id;
            this.nickname = _nickname;
            this.content = _content;
            this.date = _date;
        }

        public int getId() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public String getContent() {
            return content;
        }

        public String getNickname() {
            return nickname;
        }
    }

    private void showCustomToast(String msg, int duration) {
        //Retrieve the layout inflator
        LayoutInflater inflater = getLayoutInflater();
        //Assign the custom layout to view
        //Parameter 1 - Custom layout XML
        //Parameter 2 - Custom layout ID present in linearlayout tag of XML
        View layout = inflater.inflate(R.layout.toast_custom, (ViewGroup) findViewById(R.id.llayout_custom_toast));
        TextView msgView = (TextView) layout.findViewById(R.id.tv_toast);
        msgView.setText(msg);
        //Return the application context
        Toast toast = new Toast(getApplicationContext());
        ////Set toast gravity to bottom
        //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        //Set toast duration
        toast.setDuration(duration);
        //Set the custom layout to Toast
        toast.setView(layout);
        //Display toast
        toast.show();
    }
}
