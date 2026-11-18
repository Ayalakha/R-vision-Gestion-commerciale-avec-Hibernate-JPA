package org.example.web;

import org.example.dto.VenteCategorie;
import org.example.service.RapportVentesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * GET /rapport-ventes            -> rapport du mois précédent
 * GET /rapport-ventes?mois=2026-10 -> rapport d'un mois donné
 */
@WebServlet("/rapport-ventes")
public class RapportVentesServlet extends HttpServlet {
    private final RapportVentesService service = new RapportVentesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        YearMonth mois;
        String param = req.getParameter("mois");
        try {
            mois = (param == null || param.isBlank()) ? YearMonth.now().minusMonths(1) : YearMonth.parse(param);
        } catch (DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètre 'mois' invalide (format attendu : AAAA-MM)");
            return;
        }

        List<VenteCategorie> lignes = service.rapportMensuel(mois);
        BigDecimal total = lignes.stream().map(VenteCategorie::getChiffreAffaires).reduce(BigDecimal.ZERO, BigDecimal::add);

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Rapport des ventes</title>");
        out.println("<style>body{font-family:sans-serif;margin:2em}table{border-collapse:collapse}"
                + "td,th{border:1px solid #ccc;padding:.4em .8em}td.n{text-align:right}</style></head><body>");
        out.println("<h1>Chiffre d'affaires par catégorie — " + mois + "</h1>");
        if (lignes.isEmpty()) {
            out.println("<p>Aucune vente sur cette période.</p>");
        } else {
            out.println("<table><tr><th>Catégorie</th><th>Chiffre d'affaires</th></tr>");
            for (VenteCategorie v : lignes) {
                out.println("<tr><td>" + escape(v.getCategorie()) + "</td><td class='n'>" + v.getChiffreAffaires() + "</td></tr>");
            }
            out.println("<tr><th>Total</th><th class='n'>" + total + "</th></tr></table>");
        }
        out.println("</body></html>");
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
