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
import com.example.shipvoyage.model.Booking;

public class BookingAdapter extends ListAdapter<Booking, BookingAdapter.BookingViewHolder> {

    private OnBookingClickListener listener;

    public BookingAdapter(OnBookingClickListener listener) {
        super(new BookingDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = getItem(position);
        holder.bind(booking, listener);
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView bookingId;
        private TextView customerName;
        private TextView customerEmail;
        private TextView customerPhone;
        private TextView bookingRooms;
        private TextView bookingTotal;
        private TextView bookingDue;
        private Button cancelBookingBtn;
        private Button editBookingBtn;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            bookingId = itemView.findViewById(R.id.bookingId);
            customerName = itemView.findViewById(R.id.customerName);
            customerEmail = itemView.findViewById(R.id.customerEmail);
            customerPhone = itemView.findViewById(R.id.customerPhone);
            bookingRooms = itemView.findViewById(R.id.bookingRooms);
            bookingTotal = itemView.findViewById(R.id.bookingTotal);
            bookingDue = itemView.findViewById(R.id.bookingDue);
            cancelBookingBtn = itemView.findViewById(R.id.cancelBookingBtn);
            editBookingBtn = itemView.findViewById(R.id.editBookingBtn);
        }

        public void bind(Booking booking, OnBookingClickListener listener) {
            bookingId.setText("Booking #" + booking.getId().substring(0, Math.min(8, booking.getId().length())));
            customerName.setText(booking.getCustomerName() != null ? booking.getCustomerName() : "N/A");
            customerEmail.setText(booking.getCustomerEmail() != null ? booking.getCustomerEmail() : "N/A");
            customerPhone.setText(booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "N/A");
            bookingRooms.setText(booking.getSelectedRoomsString());
            bookingTotal.setText("$" + String.format("%.2f", booking.getPrice()));
            bookingDue.setText("$" + String.format("%.2f", booking.getDueAmount()));

            cancelBookingBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelClick(booking);
                }
            });

            editBookingBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(booking);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClick(booking);
                }
            });
        }
    }

    private static class BookingDiffCallback extends DiffUtil.ItemCallback<Booking> {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getId().equals(newItem.getId()) &&
                    oldItem.getPrice() == newItem.getPrice() &&
                    ((oldItem.getCustomerName() == null && newItem.getCustomerName() == null) || 
                     (oldItem.getCustomerName() != null && oldItem.getCustomerName().equals(newItem.getCustomerName()))) &&
                    ((oldItem.getCustomerEmail() == null && newItem.getCustomerEmail() == null) || 
                     (oldItem.getCustomerEmail() != null && oldItem.getCustomerEmail().equals(newItem.getCustomerEmail()))) &&
                    ((oldItem.getCustomerPhone() == null && newItem.getCustomerPhone() == null) || 
                     (oldItem.getCustomerPhone() != null && oldItem.getCustomerPhone().equals(newItem.getCustomerPhone())));
        }
    }

    public interface OnBookingClickListener {
        void onViewClick(Booking booking);
        void onCancelClick(Booking booking);
        void onEditClick(Booking booking);
    }
}
