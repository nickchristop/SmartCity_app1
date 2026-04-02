package com.smartcity.app.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;

import java.util.List;

/**
 * Full detail view for a selected hazard.
 * Receives all Report fields via Bundle arguments.
 */
public class HazardDetailFragment extends Fragment {

    // Bundle keys
    public static final String ARG_TITLE      = "title";
    public static final String ARG_DESC       = "desc";
    public static final String ARG_STATUS     = "status";
    public static final String ARG_LAT        = "lat";
    public static final String ARG_LNG        = "lng";
    public static final String ARG_TIMESTAMP  = "timestamp";
    public static final String ARG_SUBMITTER  = "submitter";
    public static final String ARG_IMAGES     = "images";

    /** Factory method — packages a Report into Bundle args */
    public static HazardDetailFragment newInstance(Report r) {
        HazardDetailFragment f = new HazardDetailFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TITLE,     r.getTitle());
        b.putString(ARG_DESC,      r.getDescription());
        b.putString(ARG_STATUS,    r.getStatus());
        b.putDouble(ARG_LAT,       r.getLatitude());
        b.putDouble(ARG_LNG,       r.getLongitude());
        b.putLong  (ARG_TIMESTAMP, r.getTimestamp());
        b.putString(ARG_SUBMITTER, r.getSubmitterName());
        // Pass image URL list as String array
        List<String> imgs = r.getImageUrls();
        if (imgs != null && !imgs.isEmpty()) {
            b.putStringArray(ARG_IMAGES, imgs.toArray(new String[0]));
        }
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hazard_detail, container, false);
        Bundle args = getArguments();
        if (args == null) return view;

        // Back button → pop this fragment off the stack
        view.findViewById(R.id.btn_detail_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Title
        ((TextView) view.findViewById(R.id.tv_detail_title))
                .setText(args.getString(ARG_TITLE, "—"));

        // Description
        String desc = args.getString(ARG_DESC, "");
        TextView tvDesc = view.findViewById(R.id.tv_detail_desc);
        tvDesc.setText(desc.isEmpty() ? "No description provided." : desc);

        // Status with coloured pill
        String status = args.getString(ARG_STATUS, "Pending");
        // Map "Resolved" → display as "Completed"
        String displayStatus = "Resolved".equalsIgnoreCase(status) ? "Completed" : status;
        TextView tvStatus = view.findViewById(R.id.tv_detail_status);
        tvStatus.setText(displayStatus);
        int colour;
        switch (status) {
            case "Resolved":
                colour = ContextCompat.getColor(requireContext(), R.color.status_resolved); break;
            case "In Progress":
                colour = ContextCompat.getColor(requireContext(), R.color.status_in_progress); break;
            default:
                colour = ContextCompat.getColor(requireContext(), R.color.status_pending);
        }
        tvStatus.setBackgroundTintList(ColorStateList.valueOf(colour));

        // Location
        double lat = args.getDouble(ARG_LAT, 0);
        double lng = args.getDouble(ARG_LNG, 0);
        ((TextView) view.findViewById(R.id.tv_detail_location))
                .setText(String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng));

        // Submitter
        String sub = args.getString(ARG_SUBMITTER, "Anonymous");
        ((TextView) view.findViewById(R.id.tv_detail_submitter))
                .setText(sub.isEmpty() ? "Anonymous" : sub);

        // Timestamp
        long ts = args.getLong(ARG_TIMESTAMP, 0);
        ((TextView) view.findViewById(R.id.tv_detail_time))
                .setText(DateFormat.format("MMM dd yyyy, HH:mm", ts));

        // Images (if any)
        String[] imageUrls = args.getStringArray(ARG_IMAGES);
        if (imageUrls != null && imageUrls.length > 0) {
            view.findViewById(R.id.card_detail_images).setVisibility(View.VISIBLE);
            LinearLayout imgContainer = view.findViewById(R.id.ll_detail_images);
            int dpSize = (int) (120 * getResources().getDisplayMetrics().density);
            int marginDp = (int) (8 * getResources().getDisplayMetrics().density);
            for (String url : imageUrls) {
                ImageView iv = new ImageView(requireContext());
                LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(dpSize, dpSize);
                lp.setMarginEnd(marginDp);
                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Load image using Glide (added to dependencies)
                Glide.with(this).load(url).into(iv);
                imgContainer.addView(iv);
            }
        }

        return view;
    }
}
