Moderator To Admin App Conversion

    Android Manifest
    Rename .ui.auth.MainActivity to .ui.authMainActivity2 -> First Appearance
    Rename .ui.auth.MainActivity2 to .ui.authMainActivity -> Second Appearance

    Rename App Name -> Strings.xml -> Admin Arpan

    fragment_home.xml -> LinearLayout Top -> visibility => visible
                      -> CardView -> visibility => visible
                      -> settingsImageView -> src => settings_icon
                      -> Moderator Panel => Admin Panel

    HomeFragment.kt settingsImageView OnClick => open SettingsActivity

    da_list_items.xml -> remove Switch
                      -> Assigned => Da Income
                      -> Completed => Due To Arpan

    DaItemRecyclerAdapter -> Assigned => Income and Completed => Due Logic Conversion
                          -> Enable Click Listeners


    cart_product_item_view / OrderOldMainItemRecyclerAdapter View -> Income Card => Visible

    fragment_orders_filter_date.xml -> Income Card Visible

    MyFirebaseIdService  ->  Comment intent MainActivity2 and Uncomment HomeActivityMain
