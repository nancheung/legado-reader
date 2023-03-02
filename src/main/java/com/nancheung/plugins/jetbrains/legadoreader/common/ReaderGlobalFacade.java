package com.nancheung.plugins.jetbrains.legadoreader.common;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ReaderGlobalFacade implements IReader {
    
    private static final ReaderGlobalFacade INSTANCE = new ReaderGlobalFacade();
    
    private ReaderGlobalFacade() {
    }
    
    public static ReaderGlobalFacade getInstance() {
        return INSTANCE;
    }
    
    @Override
    public void previousPage() {
        execute(IReader::previousPage);
    }
    
    @Override
    public void nextPage() {
        execute(IReader::nextPage);
    }
    
    @Override
    public void previousChapter() {
        execute(IReader::previousChapter);
    }
    
    @Override
    public void nextChapter() {
        execute(IReader::nextChapter);
    }
    
    @Override
    public void splitChapter(String chapterContent, int pageSize) {
        execute(reader -> reader.splitChapter(chapterContent, pageSize));
    }
    
    private void execute(Consumer<IReader> consumer) {
        for (ReaderFactory readerFactory : ReaderFactory.READER_FACTORYS) {
            CompletableFuture.runAsync(() -> consumer.accept(readerFactory.getReader()));
        }
    }
}
