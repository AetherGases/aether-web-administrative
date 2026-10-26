<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Funcionários | Aether</title>

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
        <h3>
            <c:out value="${empresa.name}" />
        </h3>
        <p>ADMIN</p>
    </div>

    <nav>
        <ul>
            <li><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
            <li><a href="${pageContext.request.contextPath}/funcionarios">Funcionários</a></li>
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
    <h2>Área restrita</h2>
    <p>Funcionários</p>

    <!-- Mensagens do servlet -->
    <c:if test="${not empty erro}">
        <p role="alert">
            <c:out value="${erro}" />
        </p>
    </c:if>

    <c:if test="${not empty sucesso}">
        <p role="status">
            <c:out value="${sucesso}" />
        </p>
    </c:if>

    <div>
        <button type="button" id="btnAbrirModal">
            Adicionar novo
        </button>

        <img src="${pageContext.request.contextPath}/Imagens/Mais.svg"
             alt="">
    </div>

    <!-- Modal de cadastro -->
    <div id="modalFuncionario"
         <c:if test="${not empty erro}">class="ativo"</c:if>>

        <div>
            <h2>Adicionar funcionário</h2>

            <button type="button" id="btnFecharModal"
                    aria-label="Fechar formulário">
                ✕
            </button>

            <form action="${pageContext.request.contextPath}/funcionarios"
                  method="post"
                  accept-charset="UTF-8">

                <!-- Nome -->
                <label for="nomeFuncionario">Nome completo</label>
                <input type="text"
                       id="nomeFuncionario"
                       name="campoNomeFuncionario"
                       maxlength="255"
                       value="<c:out value='${campoNomeFuncionario}'/>"
                       placeholder="Nome completo"
                       required>

                <!-- E-mail -->
                <label for="email">E-mail</label>
                <input type="email"
                       id="email"
                       name="campoEmail"
                       maxlength="255"
                       value="<c:out value='${campoEmail}'/>"
                       placeholder="E-mail"
                       required>

                <!-- Telefone -->
                <label for="telefone">Telefone</label>
                <input type="tel"
                       id="telefone"
                       name="campoTelefone"
                       value="<c:out value='${campoTelefone}'/>"
                       placeholder="Telefone"
                       required>

                <!-- CPF -->
                <label for="cpf">CPF</label>
                <input type="text"
                       id="cpf"
                       name="campoCPF"
                       value="<c:out value='${campoCPF}'/>"
                       placeholder="CPF"
                       required>

                <!-- Unidade -->
                <label for="unitId">Unidade</label>
                <select id="unitId" name="unitId" required>
                    <option value="">Selecione uma unidade</option>

                    <c:forEach var="unidade" items="${unidades}">
                        <option value="${unidade.id}"
                                <c:if test="${campoUnidade eq unidade.id.toString()}">
                                    selected
                                </c:if>>
                            <c:out value="${unidade.name}" />
                        </option>
                    </c:forEach>
                </select>

                <!-- Setor -->
                <label for="sectorId">Setor</label>
                <select id="sectorId" name="sectorId" required>
                    <option value="">Selecione um setor</option>

                    <c:forEach var="setor" items="${setores}">
                        <option value="${setor.id}"
                                <c:if test="${campoSetor eq setor.id.toString()}">
                                    selected
                                </c:if>>
                            <c:out value="${setor.name}" />
                        </option>
                    </c:forEach>
                </select>

                <!-- Senha -->
                <label for="senha">Senha</label>
                <input type="password"
                       id="senha"
                       name="campoSenha"
                       maxlength="1024"
                       placeholder="Senha"
                       autocomplete="new-password"
                       required>

                <!-- Confirmação da senha -->
                <label for="confirmarSenha">Confirmar senha</label>
                <input type="password"
                       id="confirmarSenha"
                       name="campoConfirmarSenha"
                       maxlength="1024"
                       placeholder="Confirmar senha"
                       autocomplete="new-password"
                       required>

                <!-- Status -->
                <label for="status">Status</label>
                <select name="status" id="status" required>
                    <option value="">Selecione um status</option>

                    <option value="Ativo"
                            <c:if test="${campoStatus eq 'Ativo'}">
                                selected
                            </c:if>>
                        Ativo
                    </option>

                    <option value="Inativo"
                            <c:if test="${campoStatus eq 'Inativo'}">
                                selected
                            </c:if>>
                        Inativo
                    </option>

                    <option value="Em recesso"
                            <c:if test="${campoStatus eq 'Em recesso'}">
                                selected
                            </c:if>>
                        Em recesso
                    </option>
                </select>

                <button type="submit">
                    Adicionar funcionário
                </button>

            </form>
        </div>
    </div>

    <!-- Tabela de funcionários -->
    <section>
        <table>
            <thead>
            <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Status</th>
                <th>Setor</th>
                <th>Ações</th>
            </tr>
            </thead>

            <tbody>
            <!-- Esta linha ainda é apenas um exemplo estático. -->
            <tr>
                <td>Caio Campelo Rocha</td>
                <td>caio.rocha@gmail.com</td>
                <td>Ativo</td>
                <td>I. Têxtil</td>
                <td>
                    <button type="button">Visualizar</button>
                    <button type="button">Editar</button>
                    <button type="button">Excluir</button>
                </td>
            </tr>
            </tbody>
        </table>
    </section>
</main>

<script>
    const btnAbrir = document.getElementById("btnAbrirModal");
    const btnFechar = document.getElementById("btnFecharModal");
    const modal = document.getElementById("modalFuncionario");

    btnAbrir.addEventListener("click", function () {
        modal.classList.add("ativo");
    });

    btnFechar.addEventListener("click", function () {
        modal.classList.remove("ativo");
    });
</script>

</body>
</html>