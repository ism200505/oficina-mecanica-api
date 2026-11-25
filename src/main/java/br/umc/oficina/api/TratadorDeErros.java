package br.umc.oficina.api;

import br.umc.oficina.aplicacao.CredenciaisInvalidasException;
import br.umc.oficina.aplicacao.LoginBloqueadoException;
import br.umc.oficina.dominio.comum.ConflitoException;
import br.umc.oficina.dominio.comum.RecursoNaoEncontradoException;
import br.umc.oficina.dominio.comum.RegraDeNegocioException;
import br.umc.oficina.infra.firebase.FalhaDePersistenciaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converte exceções em respostas HTTP padronizadas (RFC 7807 - Problem Details).
 * Nunca expõe stack trace ao cliente (mitigação de vazamento de informação).
 */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException e) {
        return problema(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail credenciais(CredenciaisInvalidasException e) {
        return problema(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(LoginBloqueadoException.class)
    public ProblemDetail bloqueado(LoginBloqueadoException e) {
        return problema(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail acessoNegado(AccessDeniedException e) {
        return problema(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(erro -> erro.getField(),
                        erro -> String.valueOf(erro.getDefaultMessage()), (a, b) -> a));
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos");
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail corpoInvalido(Exception e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição mal formada: verifique o JSON e os valores permitidos");
    }

    @ExceptionHandler(FalhaDePersistenciaException.class)
    public ProblemDetail banco(FalhaDePersistenciaException e) {
        LOG.error("Erro no banco de dados", e);
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "Banco de dados indisponível no momento");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception e) {
        if (e instanceof ErrorResponse erroPadraoDoSpring) {
            return erroPadraoDoSpring.getBody();
        }
        LOG.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
    }

    private static ProblemDetail problema(HttpStatus status, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(status.getReasonPhrase());
        return problema;
    }
}
