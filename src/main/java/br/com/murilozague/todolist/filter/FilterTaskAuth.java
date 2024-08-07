2package br.com.murilozague.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.murilozague.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired

    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                var servletPath = request.getServletPath();

                if(servletPath.startsWith("/tasks/")){ // se for do "tasks"

                    // 1. Pegar info. usuario e senha
                var authorization = request.getHeader("Authorization");
                var authEncoded = authorization.substring("Basic".length()).trim();
                byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                var authString = new String(authDecode);
                // ["usuario", "senha"]
                String[] credentials = authString.split(":");
                String username = credentials[0];
                String password = credentials[1];
                
                // 2. Validar usuário
                var user = this.userRepository.findByUsername(username);

                if(user == null){
                    response.sendError(401, "Usuário sem autorização");
                }else{
                    // 3. Validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                if(passwordVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    // 4. Verificação concluída
                    filterChain.doFilter(request, response);
                }else{
                    // 4. Verificação concluida (erro)
                    response.sendError(401, "Usuário sem autorização");
                }
            }
                }else{ // se não for do "tasks"
                    // 4. Verificação concluida
                    filterChain.doFilter(request, response);
                }
    }
}
