package com.ucu.ticketing.service;

import com.ucu.ticketing.dto.request.LoginRequest;
import com.ucu.ticketing.dto.request.RegisterRequest;
import com.ucu.ticketing.dto.response.AuthResponse;
import com.ucu.ticketing.exception.BusinessException;
import com.ucu.ticketing.model.Usuario;
import com.ucu.ticketing.repository.DireccionRepository;
import com.ucu.ticketing.repository.DocumentoRepository;
import com.ucu.ticketing.repository.UsuarioGeneralRepository;
import com.ucu.ticketing.repository.UsuarioRepository;
import com.ucu.ticketing.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioGeneralRepository usuarioGeneralRepository;
    private final DocumentoRepository documentoRepository;
    private final DireccionRepository direccionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
                        UsuarioGeneralRepository usuarioGeneralRepository,
                        DocumentoRepository documentoRepository,
                        DireccionRepository direccionRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioGeneralRepository = usuarioGeneralRepository;
        this.documentoRepository = documentoRepository;
        this.direccionRepository = direccionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (usuarioRepository.existsByMail(request.getMail())) {
            throw new BusinessException("Ya existe un usuario con ese mail", HttpStatus.CONFLICT);
        }

        boolean documentoExistente = documentoRepository.existsByDocumento(
                request.getDocPais(), request.getDocTipo(), request.getDocNumero());
        if (documentoExistente) {
            throw new BusinessException("Ya existe un usuario con ese documento", HttpStatus.CONFLICT);
        }

        Long idDocumento = documentoRepository.insert(
                request.getDocPais(), request.getDocTipo(), request.getDocNumero());

        Long idDireccion = direccionRepository.insert(
                request.getDirPais(), request.getDirLocalidad(), request.getDirCalle(),
                request.getDirNumero(), request.getDirCodPostal());

        String passwordHasheado = passwordEncoder.encode(request.getPassword());

        Usuario usuario = new Usuario();
        usuario.setIdDocumento(idDocumento);
        usuario.setIdDireccion(idDireccion);
        usuario.setMail(request.getMail());
        usuario.setPassword(passwordHasheado);

        Long idUsuario = usuarioRepository.insert(usuario);

        for (String telefono : request.getTelefonos()) {
            usuarioRepository.insertTelefono(idUsuario, telefono);
        }

        usuarioGeneralRepository.insert(idUsuario);

        String token = jwtUtil.generateToken(usuario.getMail(), "USUARIO");

        return new AuthResponse(token, usuario.getMail(), "USUARIO");
    }

    public AuthResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository.findByMail(request.getMail());

        if (usuario == null) {
            throw new BusinessException("Mail o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        boolean passwordCorrecto = passwordEncoder.matches(request.getPassword(), usuario.getPassword());

        if (!passwordCorrecto) {
            throw new BusinessException("Mail o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        }

        String rol = usuarioRepository.findRolByIdUsuario(usuario.getIdUsuario());

        if (rol == null) {
            throw new BusinessException(
                    "El usuario no tiene un rol asignado", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String token = jwtUtil.generateToken(usuario.getMail(), rol);

        return new AuthResponse(token, usuario.getMail(), rol);
    }
}