package server.netty_server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import server.ContextStoreService;
import server.autorization.AuthorizationService;
import server.errors.LoginIsNotAvailableException;
import server.errors.NicknameIsNotAvailableException;
import server.errors.UserNotFoundException;
import server.errors.WrongCredentialsException;
import server.model.*;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path currentPath;
    private AuthorizationService dataBaseAuthService;
    private String nickName;
    private final ContextStoreService contextStoreService;

    public AbstractMessageHandler(AuthorizationService authService,ContextStoreService contextStoreService) {
        this.dataBaseAuthService = authService;
        this.contextStoreService = contextStoreService;
        currentPath = Paths.get("serverFiles");
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage message) throws Exception {
        log.debug("received: {}", message);
        switch (message.getMessageType()) {
            case AUTH:
                AuthMessage auth = (AuthMessage) message;
                try {
                    nickName = dataBaseAuthService.getNicknameByLoginAndPassword(auth.getLogin(),auth.getPassword());
                    currentPath = currentPath.resolve(nickName);
                    ctx.writeAndFlush(new AuthOk());
                    System.out.println(currentPath.toString());
                    ctx.writeAndFlush(new FilesList(currentPath));
                } catch (UserNotFoundException e) {
                    ctx.writeAndFlush(new ErrorMessage("User Not Found"));
                }catch (WrongCredentialsException e){
                    ctx.writeAndFlush(new ErrorMessage("Wrong Credentials"));
                }
                break;
            case REGISTRATION:
                RegistrationAuth reg = (RegistrationAuth) message;
                try {
                    dataBaseAuthService.createNewUser(reg.getLogin(), reg.getPassword(), reg.getNickName());
                    Files.createDirectory(currentPath.resolve(reg.getNickName()));
                } catch (LoginIsNotAvailableException e) {
                    ctx.writeAndFlush(new ErrorMessage("Login is Not Available"));
                } catch (NicknameIsNotAvailableException e) {
                    ctx.writeAndFlush(new ErrorMessage("Nickname is Not Available"));
                }
                break;
            case FILE_REQUEST:
                FileRequest req = (FileRequest) message;
                ctx.writeAndFlush(
                        new FileMessage(currentPath.resolve(req.getFileName()))
                );
                break;
            case FILE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(
                        currentPath.resolve(fileMessage.getFileName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client connected");
        contextStoreService.registerContext(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client disconnected");
        contextStoreService.removeContext(ctx);
    }

}