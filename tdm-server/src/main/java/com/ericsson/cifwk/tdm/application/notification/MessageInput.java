package com.ericsson.cifwk.tdm.application.notification;

import java.util.List;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.MsgTemplate;

/**
 * The information in the notification email differs depending on the stage in the approval process.
 * This interface and it's implementations separates each email and allows them to evolve separately
 * and not risk affecting each other.
 */
interface MessageInput {

    MsgTemplate getBodyTemplate();

    List<User> getTo();

    String getInstigator();
}
