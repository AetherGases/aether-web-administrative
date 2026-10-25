
package com.example.servelet1ano.servlet.register;

import com.example.servelet1ano.dao.AddressesDAO;
import com.example.servelet1ano.dao.CompaniesDAO;
import com.example.servelet1ano.dao.UnitsDAO;
import com.example.servelet1ano.filter.UnitsFilter;
import com.example.servelet1ano.model.Addresses;
import com.example.servelet1ano.model.Companies;
import com.example.servelet1ano.model.Units;
import com.example.servelet1ano.validation.Validators;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/unidades")
public class UnitsRegisterServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(UnitsRegisterServlet.class.getName());

    private static final String CADASTRO = "/Units.jsp";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        int companyId = (Integer) session.getAttribute("companyId");

        try {
            carregarDados(request, companyId);

            if ("true".equals(request.getParameter("sucesso"))) {
                request.setAttribute(
                        "sucesso",
                        "Unidade cadastrada com sucesso."
                );
            }

            request.getRequestDispatcher(CADASTRO)
                    .forward(request, response);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao carregar as unidades.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Não foi possível carregar os dados das unidades."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("employeeId") instanceof Integer)
                || !(session.getAttribute("companyId") instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/Login.jsp"
            );
            return;
        }

        int companyId = (Integer) session.getAttribute("companyId");

        String nome = request.getParameter("campoNomeUnidade");
        String cnpj = request.getParameter("campoCNPJ");
        String cnae = request.getParameter("campoCNAE");
        String cep = request.getParameter("campoCEP");
        String rua = request.getParameter("campoRua");
        String numero = request.getParameter("campoNumero");
        String complemento = request.getParameter("campoComplemento");
        String bairro = request.getParameter("campoBairro");
        String cidade = request.getParameter("campoCidade");
        String estado = request.getParameter("campoEstado");
        String pais = request.getParameter("campoPais");


        request.setAttribute("campoNomeUnidade", nome);
        request.setAttribute("campoCNPJ", cnpj);
        request.setAttribute("campoCNAE", cnae);
        request.setAttribute("campoCEP", cep);
        request.setAttribute("campoRua", rua);
        request.setAttribute("campoNumero", numero);
        request.setAttribute("campoComplemento", complemento);
        request.setAttribute("campoBairro", bairro);
        request.setAttribute("campoCidade", cidade);
        request.setAttribute("campoEstado", estado);
        request.setAttribute("campoPais", pais);

        if (nome == null || nome.isBlank()
                || cnpj == null || cnpj.isBlank()
                || cnae == null || cnae.isBlank()
                || rua == null || rua.isBlank()
                || numero == null || numero.isBlank()
                || bairro == null || bairro.isBlank()
                || cidade == null || cidade.isBlank()
                || estado == null || estado.isBlank()
                || pais == null || pais.isBlank()) {

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Todos os campos são obrigatórios."
            );
            return;
        }

        nome = nome.trim();
        cnpj = cnpj.trim();
        cnae = cnae.trim();
        cep = cep.trim();
        rua = rua.trim();
        numero = numero.trim();
        complemento = complemento.trim();
        bairro = bairro.trim();
        cidade = cidade.trim();
        estado = estado.trim();
        pais = pais.trim();

        if (nome.length() > 255) {
            exibirErro(
                    request, response, companyId,
                    "O nome da unidade é muito longo."
            );
            return;
        }

        if (!Validators.isCnpj(cnpj)) {
            exibirErro(
                    request, response, companyId,
                    "O CNPJ informado não é válido."
            );
            return;
        }

        try {
            Companies empresa =
                    new CompaniesDAO().searchById(companyId);

            if (empresa == null) {
                exibirErro(
                        request, response, companyId,
                        "A empresa vinculada à sessão não foi encontrada."
                );
                return;
            }


            Addresses endereco = new Addresses();

            endereco.setZipCode(cep);
            endereco.setStreet(rua);
            endereco.setNumber(numero);
            endereco.setComplement(complemento);
            endereco.setNeighborhood(bairro);
            endereco.setCity(cidade);
            endereco.setState(estado);
            endereco.setCountry(pais);

            int addressId = new AddressesDAO().registerReturningId(endereco);

            if (addressId <= 0) {
                exibirErro(
                        request, response, companyId,
                        "Não foi possível cadastrar o endereço. Confira o CEP."
                );
                return;
            }

            Units unidade = new Units(nome, cnpj, cnae, true);
            unidade.setCompanyId(companyId);
            unidade.setAddressId(addressId);

            boolean cadastrada = new UnitsDAO().register(unidade);

            if (!cadastrada) {
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Não foi possível cadastrar a unidade."
                );
                return;
            }


            response.sendRedirect(
                    request.getContextPath() + "/unidades?sucesso=true"
            );
            return;

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao cadastrar a unidade.",
                    e
            );

            exibirErro(
                    request,
                    response,
                    companyId,
                    "Ocorreu um erro ao cadastrar a unidade. Tente novamente."
            );
        }
    }

    private void carregarDados(
            HttpServletRequest request,
            int companyId
    ) {
        Companies empresa =
                new CompaniesDAO().searchById(companyId);

        UnitsFilter filtro = new UnitsFilter();
        filtro.setCompanyId(companyId);

        List<Units> unidades =
                new UnitsDAO().searchAll(filtro);

        request.setAttribute("empresa", empresa);
        request.setAttribute("unidades", unidades);
    }

    private void exibirErro(
            HttpServletRequest request,
            HttpServletResponse response,
            int companyId,
            String mensagem
    ) throws ServletException, IOException {

        request.setAttribute("erro", mensagem);

        try {
            carregarDados(request, companyId);

        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Erro ao recarregar os dados do formulário.",
                    e
            );

            request.setAttribute("unidades", List.of());
        }

        request.getRequestDispatcher(CADASTRO)
                .forward(request, response);
    }
}
