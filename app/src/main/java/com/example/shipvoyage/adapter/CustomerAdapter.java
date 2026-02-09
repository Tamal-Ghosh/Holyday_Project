package com.example.shipvoyage.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shipvoyage.R;
import com.example.shipvoyage.model.User;
public class CustomerAdapter extends ListAdapter<User, CustomerAdapter.CustomerViewHolder> {
    private OnCustomerClickListener listener;
    public CustomerAdapter(OnCustomerClickListener listener) {
        super(new CustomerDiffCallback());
        this.listener = listener;
    }
    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        User customer = getItem(position);
        holder.bind(customer, listener);
    }
    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        private TextView roomType;
        private TextView roomNumber;
        private TextView customerName;
        private TextView adultCount;
        private TextView childCount;
        private TextView customerEmail;
        private TextView customerPhone;
        private TextView customerPayment;
        private Button viewBtn;
        private Button deleteBtn;
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            roomType = itemView.findViewById(R.id.roomType);
            roomNumber = itemView.findViewById(R.id.roomNumber);
            customerName = itemView.findViewById(R.id.customerName);
            adultCount = itemView.findViewById(R.id.adultCount);
            childCount = itemView.findViewById(R.id.childCount);
            customerEmail = itemView.findViewById(R.id.customerEmail);
            customerPhone = itemView.findViewById(R.id.customerPhone);
            customerPayment = itemView.findViewById(R.id.customerPayment);
            viewBtn = itemView.findViewById(R.id.viewBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
        public void bind(User customer, OnCustomerClickListener listener) {
            if (roomType != null) {
                roomType.setText(customer.getRoomType() != null ? customer.getRoomType() : "Room Type");
            }
            if (roomNumber != null) {
                roomNumber.setText(customer.getRoomNumber() != null ? customer.getRoomNumber() : "N/A");
            }
            if (adultCount != null) {
                adultCount.setText(String.valueOf(Math.max(0, customer.getAdultCount())));
            }
            if (childCount != null) {
                childCount.setText(String.valueOf(Math.max(0, customer.getChildCount())));
            }
            
            customerName.setText(customer.getName() != null ? customer.getName() : "N/A");
            customerEmail.setText(customer.getEmail() != null ? customer.getEmail() : "N/A");
            customerPhone.setText(customer.getPhone() != null ? customer.getPhone() : "N/A");
            customerPayment.setText(customer.getPaymentStatus() != null ? customer.getPaymentStatus() : "Pending");
            viewBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClick(customer);
                }
            });
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(customer);
                }
            });
        }
    }
    private static class CustomerDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            if (oldItem.getId() == null && newItem.getId() == null) {
                return oldItem == newItem;
            }
            if (oldItem.getId() == null || newItem.getId() == null) {
                return false;
            }
            return oldItem.getId().equals(newItem.getId());
        }
        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
                // Compare all relevant fields with null checks
                boolean nameEqual = (oldItem.getName() == null && newItem.getName() == null) ||
                    (oldItem.getName() != null && oldItem.getName().equals(newItem.getName()));
            
                boolean emailEqual = (oldItem.getEmail() == null && newItem.getEmail() == null) ||
                    (oldItem.getEmail() != null && oldItem.getEmail().equals(newItem.getEmail()));
            
                boolean phoneEqual = (oldItem.getPhone() == null && newItem.getPhone() == null) ||
                    (oldItem.getPhone() != null && oldItem.getPhone().equals(newItem.getPhone()));
            
                boolean instanceEqual = (oldItem.getLastInstance() == null && newItem.getLastInstance() == null) ||
                    (oldItem.getLastInstance() != null && oldItem.getLastInstance().equals(newItem.getLastInstance()));
            
                boolean paymentEqual = (oldItem.getPaymentStatus() == null && newItem.getPaymentStatus() == null) ||
                    (oldItem.getPaymentStatus() != null && oldItem.getPaymentStatus().equals(newItem.getPaymentStatus()));
            
                return nameEqual && emailEqual && phoneEqual && instanceEqual && paymentEqual;
        }
    }
    public interface OnCustomerClickListener {
        void onViewClick(User customer);
        void onDeleteClick(User customer);
    }
}
