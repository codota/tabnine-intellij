package com.tabnine;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TabNineCompletionContributor extends CompletionContributor {
    private static final int ADVERTISEMENT_MAX_LENGTH = 100;

    TabNineProcess proc;

    TabNineCompletionContributor() {
        new Thread(() -> {
            for (boolean first = true;; first = false) {
                try {
                    TabNineProcess proc = new TabNineProcess();
                    synchronized (this) {
                        this.proc = proc;
                    }
                    break;
                } catch (IOException e) {
                    PluginManager.processException(e);
                    try {
                        Thread.sleep(10_000);
                    } catch (InterruptedException e2) {
                        break;
                    }
                }
            }
        }).start();
    }

    static class TabNineWeigher extends LookupElementWeigher {
        TabNineWeigher() {
            super("TabNineLookupElementWeigher", false, true);
        }

        @Override
        public Integer weigh(LookupElement element) {
            if (element instanceof TabNineLookupElement) {
                TabNineLookupElement tElement = (TabNineLookupElement) element;
                return tElement.index;
            }
            return Integer.MAX_VALUE;
        }
    }

    static class TabNineLookupElement extends LookupElement {
        public final String oldPrefix;
        public final String newPrefix;
        public final String oldSuffix;
        public final String newSuffix;
        public final int index;
        String detail = null;
        boolean deprecated = false;

        public TabNineLookupElement(int index, String oldPrefix, String newPrefix, String oldSuffix, String newSuffix) {
            this.index = index;
            this.oldPrefix = oldPrefix;
            this.newPrefix = newPrefix;
            this.oldSuffix = oldSuffix;
            this.newSuffix = newSuffix;
        }

        @Override
        @NotNull
        public String getLookupString() {
            return this.newPrefix;
        }

        @Override
        public void handleInsert(@NotNull InsertionContext context) {
            int end = context.getTailOffset();
            context.getDocument().insertString(end + this.oldSuffix.length(), this.newSuffix);
            context.getDocument().deleteString(end, end + this.oldSuffix.length());
        }

        @Override
        public void renderElement(LookupElementPresentation presentation) {
            if (this.detail != null) {
                presentation.setTypeText(this.detail);
            }
            presentation.setItemTextBold(true);
            presentation.setStrikeout(this.deprecated);
            presentation.setItemText(this.newPrefix);
        }

        void copyLspFrom(TabNineProcess.ResultEntry r) {
            this.detail = r.detail;
            if (r.deprecated != null) {
                this.deprecated = r.deprecated;
            }
        }
    }

    static class TabNinePrefixMatcher extends PrefixMatcher {
        final PrefixMatcher inner;

        TabNinePrefixMatcher(PrefixMatcher inner) {
            super(inner.getPrefix());
            this.inner = inner;
        }

        @Override
        public boolean prefixMatches(@NotNull LookupElement element) {
            if (element instanceof TabNineLookupElement) {
                return true;
            } else if (element instanceof LookupElementDecorator) {
                LookupElementDecorator decorator = (LookupElementDecorator) element;
                return prefixMatches(decorator.getDelegate());
            }
            return super.prefixMatches(element);
        }

        @Override
        public boolean isStartMatch(LookupElement element) {
            if (element instanceof  TabNineLookupElement) {
                return true;
            }
            return super.isStartMatch(element);
        }

        @Override
        public boolean prefixMatches(@NotNull String name) {
            return this.inner.prefixMatches(name);
        }

        @Override
        public PrefixMatcher cloneWithPrefix(String prefix) {
            return new TabNinePrefixMatcher(this.inner.cloneWithPrefix(prefix));
        }
    }

    TabNineProcess getProcOrPrintError() {
        synchronized (this) {
            if (this.proc == null) {
                Logger.getInstance(getClass()).info("Can't get completions because TabNine process is not started yet.");
            }
            return this.proc;
        }
    }

    TabNineProcess.AutocompleteResponse retrieveCompletions(CompletionParameters parameters, int max_num_results) {
        try {
            return ApplicationUtil.runWithCheckCanceled(() -> {
                TabNineProcess proc = this.getProcOrPrintError();
                if (proc == null) {
                    return null;
                }
                final int MAX_OFFSET = 100000; // 100 KB
                Document doc = parameters.getEditor().getDocument();
                int middle = parameters.getOffset();
                int begin = Integer.max(0, middle - MAX_OFFSET);
                int end = Integer.min(doc.getTextLength(), middle + MAX_OFFSET);
                TabNineProcess.AutocompleteRequest req = new TabNineProcess.AutocompleteRequest();
                req.before = doc.getText(new TextRange(begin, middle));
                req.after = doc.getText(new TextRange(middle, end));
                req.filename = parameters.getOriginalFile().getVirtualFile().getPath();
                req.max_num_results = max_num_results;
                req.region_includes_beginning = (begin == 0);
                req.region_includes_end = (end == doc.getTextLength());
                
                try {
                    return AppExecutorUtil.getAppExecutorService().submit(() -> proc.request(req)).get(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    return null;
                }
            }, ProgressManager.getInstance().getProgressIndicator());
        } catch (Exception e) {
            return null;
        }
    }

    static boolean endsWith(Document doc, int pos, String s) {
        int begin = pos - s.length();
        if (begin < 0 || pos > doc.getTextLength()) {
            return false;
        } else {
            String tail = doc.getText(new TextRange(begin, pos));
            return tail.equals(s);
        }
    }

    @Override
    public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
        boolean preferTabNine = !endsWith(
                parameters.getEditor().getDocument(),
                parameters.getOffset() - result.getPrefixMatcher().getPrefix().length(),
                ".");
        int baseMaxCompletions = 5;
        TabNineProcess.AutocompleteResponse completions = this.retrieveCompletions(parameters, baseMaxCompletions);
        PrefixMatcher originalMatcher = result.getPrefixMatcher();
        if (completions != null) {
            result = result.withPrefixMatcher(new TabNinePrefixMatcher(originalMatcher.cloneWithPrefix(completions.old_prefix)));
            result = result.withRelevanceSorter(CompletionSorter.defaultSorter(parameters, originalMatcher).weigh(new TabNineWeigher()));
            result.restartCompletionOnAnyPrefixChange();
            if (completions.user_message.length >= 1) {
                String details = String.join(" ", completions.user_message);
                if (details.length() > ADVERTISEMENT_MAX_LENGTH){
                    details = details.substring(0,ADVERTISEMENT_MAX_LENGTH);
                }
                result.addLookupAdvertisement(details);
            }
            if (originalMatcher.getPrefix().length() == 0 && completions.results.length == 0) {
                result.stopHere();
                return;
            }
            ArrayList<LookupElement> elements = new ArrayList<>();
            int maxCompletions = preferTabNine ? baseMaxCompletions : 1;
            for (int i = 0; i < completions.results.length && i < maxCompletions; i++) {
                TabNineLookupElement elt = new TabNineLookupElement(
                        i,
                        completions.old_prefix,
                        completions.results[i].new_prefix,
                        completions.results[i].old_suffix,
                        completions.results[i].new_suffix
                );
                elt.copyLspFrom(completions.results[i]);

                if (result.getPrefixMatcher().prefixMatches(elt)) {
                    elements.add(elt);
                }
            }
            result.addAllElements(elements);
        }
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return true;
    }
}
