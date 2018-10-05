package com.dms.service;

import com.dms.repository.IncommingMessageRepository;
import com.dms.entity.IncommingMessage;
import rx.Observable;

/**
 * Created by volodymyr on 15.12.17.
 */
public class IncommingMesageService {

    private IncommingMessageRepository incommingMessageRepository;

    public IncommingMesageService(IncommingMessageRepository incommingMessageRepository) {
        this.incommingMessageRepository = incommingMessageRepository;
    }

    public Observable<IncommingMessage> getAllMessages() {
        //Prepare query for find a message;
        return incommingMessageRepository.find(IncommingMessage.GET_ALL_QUERY);
    }

    public Observable<Boolean> saveMessages(IncommingMessage message) {
        return incommingMessageRepository.put(message);
    }

}
