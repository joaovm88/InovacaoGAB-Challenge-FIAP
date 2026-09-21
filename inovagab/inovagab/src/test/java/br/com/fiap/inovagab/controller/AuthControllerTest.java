package br.com.fiap.inovagab.controller;

import br.com.fiap.inovagab.dto.LoginDTO;
import br.com.fiap.inovagab.model.NivelAcesso;
import br.com.fiap.inovagab.model.Usuario;
import br.com.fiap.inovagab.repository.UsuarioRepository;
import br.com.fiap.inovagab.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Inicializa o MockMvc em modo isolado (Standalone), eliminando falhas de filtro de segurança
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar o Token JWT (Status 200 OK)")
    void login_deveRetornarTokenQuandoCredenciaisValidas() throws Exception {
        // Arrange (Preparação dos dados e comportamento esperado)
        LoginDTO loginDTO = new LoginDTO("operador@aguia.com.br", "123456");

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId("user-123");
        usuarioAutenticado.setNome("João Operador");
        usuarioAutenticado.setEmail("operador@aguia.com.br");
        usuarioAutenticado.setSenha("encoded-pass");
        usuarioAutenticado.setNivelAcesso(NivelAcesso.OPERADOR);

        var authSucesso = new UsernamePasswordAuthenticationToken(
                usuarioAutenticado,
                null,
                usuarioAutenticado.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authSucesso);
        when(tokenService.gerarToken(usuarioAutenticado))
                .thenReturn("mocked-jwt-token-12345");

        // Act & Assert (Disparo da chamada HTTP simulada e asserções)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token-12345"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.nivelAcesso").value("OPERADOR"));
    }
}