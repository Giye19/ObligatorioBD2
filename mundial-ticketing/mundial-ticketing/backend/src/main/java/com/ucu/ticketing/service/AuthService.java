package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.LoginRequest;
import com.ucu.ticketing.dto.request.RegisterRequest;
import com.ucu.ticketing.dto.response.AuthResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.model.UsuarioGeneral;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import com.ucu.ticketing.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// contiene la logica de registro y login de usuarios
// por ahora cubre el registro de usuario general, que es el
// caso de uso principal pedido por la consigna
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
                        UsuarioGeneralRepository usuarioGeneralRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // registra un nuevo usuario general en el sistema
    // devuelve un token jwt listo para usar, asi el usuario
    // queda logueado automaticamente despues de registrarse
    public AuthResponse register(RegisterRequest request) {

        // valida que el mail no este ya en uso
        if (usuarioRepository.existsByMail(request.getMail())) {
            throw new BusinessException("Ya existe un usuario con ese mail", HttpStatus.CONFLICT);
        }

        // valida que el documento no este ya en uso
        boolean documentoExistente = usuarioRepository.existsByDocumento(
                request.getDocPais(), request.getDocTipo(), request.getDocNumero());
        if (documentoExistente) {
            throw new BusinessException("Ya existe un usuario con ese documento", HttpStatus.CONFLICT);
        }

        // hashea el password antes de guardarlo, nunca se guarda en texto plano
        String passwordHasheado = passwordEncoder.encode(request.getPassword());

        // construye y guarda el usuario base
        Usuario usuario = new Usuario();
        usuario.setMail(request.getMail());
        usuario.setPassword(passwordHasheado);
        usuario.setRol("USUARIO");
        usuario.setDocPais(request.getDocPais());
        usuario.setDocTipo(request.getDocTipo());
        usuario.setDocNumero(request.getDocNumero());
        usuario.setDirPais(request.getDirPais());
        usuario.setDirLocalidad(request.getDirLocalidad());
        usuario.setDirCalle(request.getDirCalle());
        usuario.setDirNumero(request.getDirNumero());
        usuario.setDirCodPostal(request.getDirCodPostal());

        usuarioRepository.insert(usuario);

        // guarda cada telefono asociado
        for (String telefono : request.getTelefonos()) {
            usuarioRepository.insertTelefono(request.getMail(), telefono);
        }

        // guarda la especializacion de usuario general
        UsuarioGeneral usuarioGeneral = new UsuarioGeneral();
        usuarioGeneral.setMail(request.getMail());
        usuarioGeneral.setFechaRegistro(LocalDate.now());
        usuarioGeneral.setEstadoVerificacion("PENDIENTE");

        usuarioGeneralRepository.insert(usuarioGeneral);

        // genera el token jwt para que quede logueado automaticamente
        String token = jwtUtil.generateToken(usuario.getMail(), usuario.getRol());

        return new AuthResponse(token, usuario.getMail(), usuario.getRol());
    }

    // valida las credenciales de un usuario y devuelve un token jwt
    // valido para cualquier rol (admin, funcionario, usuario general)
    public AuthResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByMail(request.getMail());

        if (usuario == null) {
            throw new BusinessException("Mail o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        // compara el password ingresado contra el hash guardado
        boolean passwordCorrecto = passwordEncoder.matches(request.getPassword(), usuario.getPassword());

        if (!passwordCorrecto) {
            throw new BusinessException("Mail o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(usuario.getMail(), usuario.getRol());

        return new AuthResponse(token, usuario.getMail(), usuario.getRol());
    }
}