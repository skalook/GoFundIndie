package com.IndieAn.GoFundIndie.Resolvers.Mutations;

import com.IndieAn.GoFundIndie.Domain.Entity.Board;
import com.IndieAn.GoFundIndie.Domain.Entity.User;
import com.IndieAn.GoFundIndie.Repository.BoardLikeRepository;
import com.IndieAn.GoFundIndie.Repository.BoardRepository;
import com.IndieAn.GoFundIndie.Repository.UserRepository;
import com.IndieAn.GoFundIndie.Resolvers.DTO.Board.*;
import com.IndieAn.GoFundIndie.Resolvers.DTO.OnlyCodeDTO;
import com.IndieAn.GoFundIndie.Service.BoardService;
import com.IndieAn.GoFundIndie.Service.UserService;
import graphql.kickstart.servlet.context.GraphQLServletContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardMutation {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardLikeRepository boardLikeRepository;

    private final BoardService boardService;
    private final UserService userService;

    private int envValidCheck(DataFetchingEnvironment env) {
        GraphQLServletContext context = env.getContext();
        HttpServletRequest request = context.getHttpServletRequest();
        String accessToken = request.getHeader("accesstoken");

        // No token in the Header : 4000
        if(accessToken == null) return 4000;

        Map<String, Object> checkToken = userService.CheckToken(accessToken);

        if(checkToken.get("email") == null)
            return Integer.parseInt(checkToken.get("code").toString());
        else
            return 0;
    }

    private User findUser(DataFetchingEnvironment env) {
        GraphQLServletContext context = env.getContext();
        HttpServletRequest request = context.getHttpServletRequest();

        return userService.FindUserUseEmail(
                userService.CheckToken(request
                        .getHeader("accesstoken"))
                        .get("email").toString());
    }

    public WrappingCreateTempBoardDTO CreateTempBoard(DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if(code == 0) {
                return WrappingCreateTempBoardDTO.builder().code(2000)
                        .data(CreateTempBoardDTO.builder()
                                .id(boardRepository.RegisterTempBoard(findUser(env)))
                                .build())
                        .build();
            } else
                return WrappingCreateTempBoardDTO.builder().code(code).build();

            // Test Code : No Access Token
//            return WrappingCreateTempBoardDTO.builder()
//                    .code(2000)
//                    .data(CreateTempBoardDTO.builder().id(boardRepository
//                            .RegisterTempBoard(userRepository.FindUserByIdDB(1L))).build())
//                    .build();

        } catch (NullPointerException e) {
            return WrappingCreateTempBoardDTO.builder().code(4000).build();
        }
    }

    public WrappingCreateTempBoardDTO CompleteBoard(CreateBoardCompleteDTO dto, DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if(code == 0) {
                Board board = boardRepository.findBoardId(dto.getBoardId());
                // Can not find board : 4401
                if(board == null)
                    return WrappingCreateTempBoardDTO.builder().code(4401).build();
                else if(board.getCastings().stream().noneMatch(el -> el.getPosition() == 1))
                    // Can not find Director : 4403
                    return WrappingCreateTempBoardDTO.builder().code(4403).build();
                else if(board.getBoardGenres().size() == 0)
                    // Can not find Genre : 4404
                    return WrappingCreateTempBoardDTO.builder().code(4404).build();

                User user = findUser(env);
                // Invalid User : 4301
                if(!user.isAdminRole() && user.getId() != board.getUserId().getId())
                    return WrappingCreateTempBoardDTO.builder().code(4301).build();

                try {
                    // User != Admin : 4300
                    if(board.isApprove() && !user.isAdminRole()) {
                        return WrappingCreateTempBoardDTO.builder()
                                .code(4300)
                                .build();
                    } else {
                        return WrappingCreateTempBoardDTO.builder()
                                .code(2000)
                                .data(CreateTempBoardDTO.builder()
                                        .id(boardRepository.CompleteBoard(board, dto)).build())
                                .build();
                    }
                } catch (NullPointerException e) {
                    // Essential value is null : 4006
                    return WrappingCreateTempBoardDTO.builder()
                            .code(4006).build();
                }
            } else {
                // Token Invalid
                return WrappingCreateTempBoardDTO.builder().code(code).build();
            }

            // Test Code : No Access Token
//            return WrappingCreateTempBoardDTO.builder()
//                    .code(2000)
//                    .data(CreateTempBoardDTO.builder().id(boardRepository
//                            .CompleteBoard(dto).getId()).build())
//                    .build();

        } catch (NullPointerException e) {
            return WrappingCreateTempBoardDTO.builder().code(4000).build();
        }
    }

    // ! Only -- Admin --
    public WrappingCreateTempBoardDTO PutBoard(PutBoardDTO dto, DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if (code == 0) {
                Board board = boardRepository.findBoardId(dto.getBoardId());
                // Can not find board : 4401
                if (board == null)
                    return WrappingCreateTempBoardDTO.builder().code(4401).build();

                User user = findUser(env);

                // User != Admin : 4300
                if (!user.isAdminRole()) {
                    return WrappingCreateTempBoardDTO.builder()
                            .code(4300)
                            .build();
                } else {
                    return WrappingCreateTempBoardDTO.builder()
                            .code(2000)
                            .data(CreateTempBoardDTO.builder()
                                    .id(boardRepository.PutBoard(board, dto)).build())
                            .build();
                }
            } else {
                // Token Invalid
                return WrappingCreateTempBoardDTO.builder().code(code).build();
            }

            // Test Code : No Access Token
//            return WrappingCreateTempBoardDTO.builder()
//                    .code(2000)
//                    .data(CreateTempBoardDTO.builder()
//                            .id(boardRepository.PutBoard(
//                                    boardRepository.findBoardId(dto.getBoardId()), dto
//                            )).build())
//                    .build();

        } catch (NullPointerException e) {
            return WrappingCreateTempBoardDTO.builder().code(4000).build();
        }
    }

    public OnlyCodeDTO DeleteBoard(long id, DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if (code == 0) {
                Board board = boardRepository.findBoardId(id);
                if(board == null)
                    return OnlyCodeDTO.builder().code(4401).build();

                User user = findUser(env);
                if(!user.isAdminRole())
                    return OnlyCodeDTO.builder().code(4300).build();

                boardRepository.DeleteBoard(board);
                return OnlyCodeDTO.builder().code(2000).build();
            } else {
                // Token Invalid
                return OnlyCodeDTO.builder().code(code).build();
            }

            // Test Code : No Access Token
//            boardRepository.DeleteBoard(boardRepository.findBoardId(id));
//            return OnlyCodeDTO.builder().code(2000).build();

        } catch (NullPointerException e) {
            return OnlyCodeDTO.builder().code(4000).build();
        }
    }

    public OnlyCodeDTO ApproveBoard(long id, boolean isApprove, DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if (code == 0) {
                Board board = boardRepository.findBoardId(id);
                if(board == null)
                    return OnlyCodeDTO.builder().code(4401).build();

                User user = findUser(env);
                if(!user.isAdminRole())
                    return OnlyCodeDTO.builder().code(4300).build();

                boardRepository.ApproveBoard(board, isApprove);
                return OnlyCodeDTO.builder().code(2000).build();
            } else {
                // Token Invalid
                return OnlyCodeDTO.builder().code(code).build();
            }

            // Test Code : No Access Token
//            boardRepository.ApproveBoard(boardRepository.findBoardId(id), isApprove);
//            return OnlyCodeDTO.builder().code(2000).build();

        } catch (NullPointerException e) {
            return OnlyCodeDTO.builder().code(4000).build();
        }
    }

    public OnlyCodeDTO SwitchLikeBoard(long id, DataFetchingEnvironment env) {
        try {
            int code = envValidCheck(env);

            if (code == 0) {
                Board board = boardRepository.findBoardId(id);
                if(board == null)
                    return OnlyCodeDTO.builder().code(4401).build();

                boardLikeRepository.LikeBoardSwitch(findUser(env), board);
                return OnlyCodeDTO.builder().code(2000).build();
            } else {
                // Token Invalid
                return OnlyCodeDTO.builder().code(code).build();
            }
        } catch (NullPointerException e) {
            return OnlyCodeDTO.builder().code(4000).build();
        }

        // Test Code
//        boardLikeRepository.LikeBoardSwitch(
//                userRepository.FindUserByIdDB(1L),
//                boardRepository.findBoardId(36L)
//        );
//        return OnlyCodeDTO.builder().code(2000).build();
    }
}
