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

/**
 * Servlet da tela de unidades (/unidades)
 * Lista as unidades da empresa do usuario logado e cadastra novas unidades junto com o endereco
 */
@WebServlet("/unidades")
public class UnitsRegisterServlet extends HttpServlet {

    /**
     * Registro de logs usado para anotar os erros inesperados
     */
    private static final Logger LOGGER =
            Logger.getLogger(UnitsRegisterServlet.class.getName());

    /**
     * Pagina da tela de unidades
     */
    private static final String CADASTRO = "/Units.jsp";

    /**
     * metodo que mostra a tela de unidades da empresa do usuario logado
     * Se nao houver sessao valida, redireciona para o login
     * Se vier o parametro sucesso=true, mostra a mensagem de unidade cadastrada
     * @param request A requisicao (a empresa vem da sessao)
     * @param response A resposta que encaminha para a tela de unidades ou redireciona para o login
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
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

    /**
     * metodo que cadastra uma nova unidade (com o endereco) na empresa do usuario logado
     * Se nao houver sessao valida, redireciona para o login
     * Confere os campos obrigatorios, o tamanho do nome, o CNPJ e o CEP, e se a unidade ja nao foi cadastrada
     * Se algo estiver errado, volta para a tela com a mensagem de erro (mantendo o que foi digitado)
     * Se der certo, redireciona para /unidades com o aviso de sucesso
     * @param request A requisicao com os dados da unidade e do endereco (campos que comecam com "campo")
     * @param response A resposta que redireciona ou volta para a tela de unidades
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
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

        if (!Validators.isCep(cep)) {
            exibirErro(
                    request, response, companyId,
                    "O CEP informado não é válido."
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
            UnitsFilter filter = new UnitsFilter();
            filter.setName(nome);
            filter.setCompanyId(companyId);
            try {
                List<Units> existe = new UnitsDAO().searchAll(filter);
                if (!existe.isEmpty()){
                    exibirErro(
                            request,
                            response,
                            companyId,
                            "A unidade já foi cadastrada anteriormente."
                    );
                    return;
                }
            } catch (RuntimeException e) {
                LOGGER.log(
                        Level.SEVERE,
                        "Erro ao conectar com o banco"
                );
                exibirErro(
                        request,
                        response,
                        companyId,
                        "Não foi possível concluir o cadastro. Tente novamente mais tarde."
                );
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

    //    ------------- Metodos auxiliares (privados) --------------

    /**
     * Carrega os dados que a tela de unidades precisa
     * Busca a empresa do usuario e guarda no atributo "empresa"
     * Busca as unidades dessa empresa e guarda no atributo "unidades"
     * Se a consulta falhar, a excecao e repassada para quem chamou tratar o erro
     * @param request A requisicao onde os atributos serao guardados
     * @param companyId O id da empresa do usuario logado
     */
    private void carregarDados(
            HttpServletRequest request,
            int companyId
    ) {
        Companies empresa =
                new CompaniesDAO().searchById(companyId);

        UnitsFilter unitsFilter = new UnitsFilter();
        unitsFilter.setCompanyId(companyId);

        List<Units> unidades = new UnitsDAO().searchAll(unitsFilter);

        request.setAttribute("empresa", empresa);
        request.setAttribute("unidades", unidades);
    }

    /**
     * Mostra uma mensagem de erro na tela de unidades
     * Guarda a mensagem no atributo "erro", recarrega os dados da tela
     * e encaminha a mesma requisicao de volta para a pagina, assim o que o usuario digitou continua preenchido
     * Se nao for possivel recarregar os dados, a lista de unidades fica vazia
     * @param request A requisicao atual
     * @param response A resposta que encaminha para a tela de unidades
     * @param companyId O id da empresa do usuario logado
     * @param mensagem O texto do erro que sera mostrado ao usuario
     * @throws ServletException se der erro no encaminhamento para a pagina
     * @throws IOException se der erro de entrada ou saida
     */
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