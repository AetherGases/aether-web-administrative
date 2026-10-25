<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tela de Setores do Aether</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Comfortaa:wght@600&family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap"
          rel="stylesheet">

    <link rel="icon"
          href="${pageContext.request.contextPath}/Imagens/LogoAether.svg"
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
            <li><a href="${pageContext.request.contextPath}/funcionarios">Funcionários</a></li>
            <li><a href="${pageContext.request.contextPath}/setores">Setores</a></li>
            <li><a href="${pageContext.request.contextPath}/unidades">Unidades</a></li>
            <li><a href="${pageContext.request.contextPath}/empresas">Empresas</a></li>
            <li><a href="${pageContext.request.contextPath}/permissoes">Permissões</a></li>
        </ul>
    </nav>
</aside>

<main>
    <div>
        <h2>Área restrita</h2>
        <p>Setores</p>
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

    <div id="modalSetores" class="${not empty erro ? 'ativo' : ''}">
        <div>
            <h2>Adicionar Setor</h2>

            <button type="button" id="btnFecharModal" aria-label="Fechar">
                ✕
            </button>

            <form action="${pageContext.request.contextPath}/setores"
                  method="post">

                <label for="nomeSetor">Nome do setor *</label>
                <input type="text"
                       id="nomeSetor"
                       name="campoNomeSetor"
                       placeholder="Nome do setor"
                       maxlength="255"
                       value="<c:out value='${campoNomeSetor}'/>"
                       required>

                <label for="idUnidade">Unidade *</label>
                <select id="idUnidade"
                        name="campoIdUnidade"
                        required>
                    <option value="">Selecione uma unidade</option>

                    <c:forEach var="unidade" items="${unidades}">
                        <option value="${unidade.id}"
                                <c:if test="${campoIdUnidade == unidade.id}">
                                    selected
                                </c:if>>
                            <c:out value="${unidade.name}"/>
                        </option>
                    </c:forEach>
                </select>

                <label for="descricao">Descrição *</label>
                <textarea id="descricao"
                          name="campoDescricao"
                          placeholder="Descrição do setor"
                          required><c:out value="${campoDescricao}"/></textarea>

                <button type="submit">Adicionar</button>
            </form>
        </div>
    </div>

    <section>
        <table>
            <thead>
            <tr>
                <th>Setor</th>
                <th>ID</th>
                <th>Unidade</th>
                <th>Status</th>
                <th>Descrição</th>
                <th>Ações</th>
            </tr>
            </thead>

            <tbody>
            <c:choose>
                <c:when test="${not empty setores}">
                    <c:forEach var="setor" items="${setores}">
                        <tr>
                            <td>
                                <c:out value="${setor.name}"/>
                            </td>

                            <td>
                                <c:out value="${setor.id}"/>
                            </td>

                            <td>
                                <c:out value="${setor.unit.name}"/>
                            </td>

                            <td>
                                <c:choose>
                                    <c:when test="${setor.active}">
                                        Ativo
                                    </c:when>
                                    <c:otherwise>
                                        Inativo
                                    </c:otherwise>
                                </c:choose>
                            </td>

                            <td>
                                <c:out value="${setor.description}"/>
                            </td>
                            <td>
                                <a href="">Visualizar</a>
                                <a href="">Editar</a>

                                <c:if test="${setor.active}">
                                    <form action="${pageContext.request.contextPath}/setores"
                                          method="post">

                                        <input type="hidden" name="acao" value="excluir">

                                        <input type="hidden"
                                               name="id"
                                               value="<c:out value='${setor.id}'/>">

                                        <button type="submit"
                                                onclick="return confirm('Tem certeza de que deseja desativar este setor?')">
                                            Desativar
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <tr>
                        <td colspan="6">Nenhum setor encontrado.</td>
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
    const modal = document.getElementById('modalSetores');

    btnAbrir.addEventListener('click', function () {
        modal.classList.add('ativo');
    });

    btnFechar.addEventListener('click', function () {
        modal.classList.remove('ativo');
    });
</script>

</body>
</html>
