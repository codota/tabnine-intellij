package com.tabnine.binary.requests.notifications

import com.tabnine.lifecycle.GlobalActionVisitor

enum class NotificationActions {
    None {
        override fun visit(actionVisitor: GlobalActionVisitor) {
            actionVisitor.none();
        }
    },
    OpenHub {
        override fun visit(actionVisitor: GlobalActionVisitor) {
            actionVisitor.openHub();
        }
    },
    OpenLp {
        override fun visit(actionVisitor: GlobalActionVisitor) {
            actionVisitor.openLp();
        }
    },
    OpenBuy {
        override fun visit(actionVisitor: GlobalActionVisitor) {
            actionVisitor.openBuy();
        }
    };

    abstract fun visit(actionVisitor: GlobalActionVisitor)
}