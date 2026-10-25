
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tela de Unidades do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap"
          rel="stylesheet">

    <link rel="icon" href="${pageContext.request.contextPath}/Imagens/LogoAether.svg"
          type="image/svg+xml">
</head>

<body>

<aside>
    <div>
        <img src="${pageContext.request.contextPath}/Imagens/LogoAethererdeEscuro.svg"
             alt="Logo Aether">
        <p>aether</p>
    </div>

    <div>
        <h3>Área administrativa</h3>
        <p>ADMIN</p>
    </div>


    <nav>
        <ul>
            <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
            <li><a href="${pageContext.request.contextPath}/funcionarios">Funcionarios</a></li>
            <li><a href="${pageContext.request.contextPath}/setores">Setores</a></li>
            <li><a href="${pageContext.request.contextPath}/unidades">Unidades</a></li>
            <li><a href="${pageContext.request.contextPath}/empresas">Empresas</a></li>
            <li><a href="${pageContext.request.contextPath}/permissoes">Permissoes</a></li>
        </ul>
    </nav>

    <div>
    <a href="${pageContext.request.contextPath}/Login.jsp">Logout</a>
    </div>

</aside>

<main>
    <div>
        <h2>Área restrita</h2>
        <p>Unidades</p>
    </div>

    <c:if test="${not empty erro}">
        <div role="alert">
            <c:out value="${erro}"/>
        </div>
    </c:if>

    <c:if test="${not empty sucesso}">
        <div role="status">
            <c:out value="${sucesso}"/>
        </div>
    </c:if>

    <div>
        <button type="button" id="btnAbrirModal">Adicionar novo</button>
        <img src="${pageContext.request.contextPath}/Imagens/Mais.svg"
             alt="Adicionar">
    </div>

    <div id="modalUnidades" class="${not empty erro ? 'ativo' : ''}">
        <div>
            <h2>Adicionar Unidade</h2>
            <button type="button" id="btnFecharModal" aria-label="Fechar">✕</button>

            <form action="${pageContext.request.contextPath}/unidades" method="post">

                <label for="nomeUnidade">Nome da unidade *</label>
                <input type="text"
                       id="nomeUnidade"
                       name="campoNomeUnidade"
                       placeholder="Nome da unidade"
                       maxlength="255"
                       value="<c:out value='${campoNomeUnidade}'/>"
                       required>

                <label for="cnpj">CNPJ *</label>
                <input type="text"
                       id="cnpj"
                       name="campoCNPJ"
                       placeholder="CNPJ"
                       value="<c:out value='${campoCNPJ}'/>"
                       required>

                <label for="cnae">CNAE *</label>
                <input type="text"
                       id="cnae"
                       name="campoCNAE"
                       placeholder="CNAE"
                       value="<c:out value='${campoCNAE}'/>"
                       required>

                <h3>Endereço</h3>

                <label for="cep">CEP</label>
                <input type="text"
                       id="cep"
                       name="campoCEP"
                       placeholder="CEP"
                       value="<c:out value='${campoCEP}'/>">

                <label for="rua">Rua *</label>
                <input type="text"
                       id="rua"
                       name="campoRua"
                       placeholder="Rua"
                       value="<c:out value='${campoRua}'/>"
                       required>

                <label for="numero">Número *</label>
                <input type="text"
                       id="numero"
                       name="campoNumero"
                       placeholder="Número"
                       value="<c:out value='${campoNumero}'/>"
                       required>

                <label for="complemento">Complemento</label>
                <input type="text"
                       id="complemento"
                       name="campoComplemento"
                       placeholder="Complemento"
                       value="<c:out value='${campoComplemento}'/>">

                <label for="bairro">Bairro *</label>
                <input type="text"
                       id="bairro"
                       name="campoBairro"
                       placeholder="Bairro"
                       value="<c:out value='${campoBairro}'/>"
                       required>

                <label for="cidade">Cidade *</label>
                <input type="text"
                       id="cidade"
                       name="campoCidade"
                       placeholder="Cidade"
                       value="<c:out value='${campoCidade}'/>"
                       required>

                <label for="estado">Estado *</label>
                <input type="text"
                       id="estado"
                       name="campoEstado"
                       placeholder="Estado"
                       value="<c:out value='${campoEstado}'/>"
                       required>

                <label for="pais">País *</label>
                <input type="text"
                       id="pais"
                       name="campoPais"
                       placeholder="País"
                       value="<c:out value='${campoPais}'/>"
                       required>

                <button type="submit">Adicionar</button>
            </form>
        </div>
    </div>

    <section>
        <table>
            <thead>
            <tr>
                <th>Nome</th>
                <th>ID</th>
                <th>Endereço</th>
                <th>Status</th>
                <th>CNPJ</th>
                <th>CNAE</th>
                <th>Empresa</th>
            </tr>
            </thead>

            <tbody>
            <c:choose>
                <c:when test="${not empty unidades}">
                    <c:forEach var="unidade" items="${unidades}">
                        <tr>
                            <td><c:out value="${unidade.name}"/></td>
                            <td><c:out value="${unidade.id}"/></td>

                            <td>
                                <c:if test="${not empty unidade.address}">
                                    <c:out value="${unidade.address.street}"/>,
                                    <c:out value="${unidade.address.number}"/>

                                    <c:if test="${not empty unidade.address.complement}">
                                        - <c:out value="${unidade.address.complement}"/>
                                    </c:if>

                                    <br>
                                    <c:out value="${unidade.address.neighborhood}"/> -
                                    <c:out value="${unidade.address.city}"/>/
                                    <c:out value="${unidade.address.state}"/>
                                    <br>
                                    CEP: <c:out value="${unidade.address.zipCode}"/>
                                </c:if>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${unidade.active}">Ativa</c:when>
                                    <c:otherwise>Inativa</c:otherwise>
                                </c:choose>
                            </td>

                            <td><c:out value="${unidade.cnpj}"/></td>
                            <td><c:out value="${unidade.cnae}"/></td>

                            <td>
                                <c:out value="${unidade.company.name}"/>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <tr>
                        <td colspan="7">Nenhuma unidade encontrada.</td>
                    </tr>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </section>
</main>

<script>
    const btnAbrir = document.getElementById('btnAbrirModal');
    const btnFechar = document.getElementById('btnFecharModal');
    const modal = document.getElementById('modalUnidades');

    btnAbrir.addEventListener('click', function () {
        modal.classList.add('ativo');
    });

    btnFechar.addEventListener('click', function () {
        modal.classList.remove('ativo');
    });
</script>

</body>
</html>
